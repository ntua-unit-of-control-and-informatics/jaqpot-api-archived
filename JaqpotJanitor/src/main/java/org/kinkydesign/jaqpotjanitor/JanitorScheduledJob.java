/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.kinkydesign.jaqpotjanitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import org.kinkydesign.jaqpotjanitor.core.AssertionException;
import org.kinkydesign.jaqpotjanitor.core.TestResult;
import org.kinkydesign.jaqpotjanitor.core.Testable;
import org.kinkydesign.jaqpotjanitor.core.TestsBucket;
import org.reflections.Reflections;

/**
 * Runs periodic tests on JAQPOT.
 *
 * @author chung
 */
@Stateless
public class JanitorScheduledJob {

    private static Set<Class<?>> annotated;
    private static final Logger LOG = Logger.getLogger(JanitorScheduledJob.class.getName());

    private static final BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<>(200);
    private static final ExecutorService executorService = new ThreadPoolExecutor(4, 10,
            100, TimeUnit.SECONDS,
            linkedBlockingDeque,
            new ThreadPoolExecutor.AbortPolicy());

    private static final long MAX_DURATION = 30000l;

    class JobInfo {

        Long timeout;
        String name;
        String description;
        TestJob testJob;

        public JobInfo(Long timeout, String name, String description, TestJob testJob) {
            this.timeout = timeout;
            this.name = name;
            this.description = description;
            this.testJob = testJob;
        }

        public Long getTimeout() {
            return timeout;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public TestJob getTestJob() {
            return testJob;
        }

    }

    class TestJob implements Callable<TestResult> {

        final String testName;
        final String testDescription;
        final Method m;
        final Object t;
        volatile Long started = null;

        public TestJob(String testName, String testDescription, Method m, Object t) {
            this.testName = testName;
            this.testDescription = testDescription;
            this.m = m;
            this.t = t;
        }

        /**
         * Returns the timestamp when the job starts running.
         * If the job has not started yet, this method returns <code>null</code>.
         * @return timestamp of start
         */
        public Long started() {
            return this.started;
        }

        @Override
        public TestResult call() throws Exception {
            /* Once the job starts running, record its timestamp */
            started = System.currentTimeMillis();
            TestResult tr = new TestResult();
            tr.setTestName(testName);
            tr.setTestDescription(testDescription);
            long beforeTest = System.currentTimeMillis();
            try {
                m.invoke(t);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getTargetException();
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                tr.setPass(false);
                tr.setStackTrace(exceptionAsString);
                if (!(cause instanceof AssertionException)) {
                    LOG.log(Level.SEVERE, null, cause);
                }
            }
            tr.setDuration(System.currentTimeMillis() - beforeTest);
            tr.setTimestamp(beforeTest);
            return tr;
        }
    }

    /**
     * Runs all tests of a class c (annotated by {@link Testable}) and returns a
     * list of test results. A {@link TestResult} is returned by each method
     * annotated by {@link Testable}
     *
     * @param c Testable class
     * @return list of test results
     */
    private List<TestResult> runTests(Class<?> c) {

        List<TestResult> testResults = new ArrayList<>();
        Map<Future<TestResult>, JobInfo> callableTasks = new ConcurrentHashMap<>();

        try {
            LOG.log(Level.INFO, "Testing {0}", c.getSimpleName());
            final Object t = c.newInstance();
            Method[] allMethods = c.getDeclaredMethods();
            for (final Method m : allMethods) {
                if (m.isAnnotationPresent(Testable.class)) {

                    String nameFromAnnotation = m.getAnnotation(Testable.class).name();
                    final String testName = "##default".equals(nameFromAnnotation) ? m.getName() : nameFromAnnotation;
                    final String testDescription = m.getAnnotation(Testable.class).description();
                    long mMaxDuration = m.getAnnotation(Testable.class).maxDuration();
                    final long maxDuration = mMaxDuration < 0 ? MAX_DURATION : mMaxDuration;
                    TestJob tj = new TestJob(testName, testDescription, m, t);
                    Future<TestResult> future = executorService.submit(tj);
                    callableTasks.put(future, new JobInfo(maxDuration, testName, testDescription, tj));
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Improper test function.", ex);
            LOG.log(Level.SEVERE, "Test function prototype is: @Testable public void doTest();", ex);
            // this catch is fail-safe (if a test fails to be invoked, move on to the next one)
        }

        /* Now wait for all tasks to finish */
        while (callableTasks.size() > 0) { // while there are more things to do (tests are still running)            
            //Iterator<Future<TestResult>> iterator = callableTasks.iterator();
            Iterator<Map.Entry<Future<TestResult>, JobInfo>> iterator = callableTasks.entrySet().iterator();
            while (iterator.hasNext()) { // check out each task...
                try {
                    Map.Entry<Future<TestResult>, JobInfo> entry = iterator.next();

                    Future<TestResult> future = entry.getKey();
                    if (future.isDone()) { // task is over!
                        iterator.remove(); // remove from the list of TODOs
                        TestResult testResultObtained = future.get(); // obtain result
                        testResults.add(testResultObtained); // add test result to the list (test is done)
                        LOG.log(testResultObtained.isPass() ? Level.INFO : Level.SEVERE,
                                "[{0}] {1} ({2}ms)",
                                new Object[]{
                                    testResultObtained.isPass() ? "PASS" : "FAIL",
                                    testResultObtained.getTestName(),
                                    testResultObtained.getDuration()
                                });
                    }
                    if (entry.getValue() != null
                            && entry.getValue().getTestJob().started() != null
                            && (System.currentTimeMillis() - entry.getValue().getTestJob().started() > entry.getValue().getTimeout())) {
                        entry.getKey().cancel(true);
                        LOG.log(Level.SEVERE, "Test [{0}] was cancelled because it took more than {1}ms to complete!",
                                new Object[]{
                                    entry.getValue().getName(),
                                    entry.getValue().getTimeout()});

                        TestResult testResultCancelled = new TestResult();
                        testResultCancelled.setPass(false);
                        testResultCancelled.setTestName(entry.getValue().getName());
                        testResultCancelled.setTestDescription(entry.getValue().getDescription());
                        testResults.add(testResultCancelled);

                        iterator.remove();

                    }
                } catch (InterruptedException | ExecutionException ex) {
                    LOG.log(Level.SEVERE, "Incredible exception", ex);
                }
            }
        }
        return testResults;
    }

    /**
     * Scheduled job which executes all tests periodically. All test classes are
     * annotated with {@link Testable} and are found in the package
     * {@link org.kinkydesign.jaqpotjanitor.tests}. This scheduled job executes
     * all tests, gathers their results, packs them in a {@link TestsBucket}
     * object and stores it in the database. The test bucket can then be
     * identified by its ID and the {@link TestsBucket#getTimestamp() timestamp}
     * of its creation.
     */
    @Schedule(hour = "*", minute = "*", second = "*/30", info = "TestRunner", persistent = false)
    public void doScheduled() {
        LOG.info("RUNNING TESTS!");
        if (annotated == null) {
            Reflections reflections = new Reflections("org.kinkydesign.jaqpotjanitor.tests");
            annotated = reflections.getTypesAnnotatedWith(Testable.class);
        }
        List<TestResult> allTestResults = new ArrayList<>();
        for (Class<?> c : annotated) {
            allTestResults.addAll(runTests(c));
        }

        TestsBucket bucket = new TestsBucket();
        bucket.setTestResults(allTestResults);
        bucket.setTimestamp(System.currentTimeMillis());

        //TODO write bucket in DB!
    }

}
