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
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
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

    BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<>(40);
    ExecutorService executorService = new ThreadPoolExecutor(1, 4,
            100, TimeUnit.SECONDS,
            linkedBlockingDeque,
            new ThreadPoolExecutor.AbortPolicy());

    private List<TestResult> runTests(Class<?> c) {

        List<TestResult> testResults = new ArrayList<>();

        List<Future<TestResult>> callableTasks = new ArrayList<>();
        try {
            LOG.log(Level.INFO, "Testing {0}", c.getSimpleName());
            final Object t = c.newInstance();
            Method[] allMethods = c.getDeclaredMethods();
            for (final Method m : allMethods) {
                if (m.isAnnotationPresent(Testable.class)) {

                    String nameFromAnnotation = m.getAnnotation(Testable.class).name();
                    final String testName = "##default".equals(nameFromAnnotation) ? m.getName() : nameFromAnnotation;

                    Future<TestResult> future = executorService.submit(new Callable<TestResult>() {

                        @Override
                        public TestResult call() throws Exception {
                            TestResult tr = new TestResult();
                            tr.setTestName(testName);
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

                    });
                    callableTasks.add(future);
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Improper test function.", ex);
            LOG.log(Level.SEVERE, "Test function prototype is: @Testable public void doTest();", ex);
            // this catch is fail-safe (if a test fails to be invoked, move on to the next one)
        }

        /**
         * Now wait for all tasks to finish.
         */

        while (callableTasks.size() > 0) { // while there are more things to do (tests are still running)            
            Iterator<Future<TestResult>> iterator = callableTasks.iterator();
            while (iterator.hasNext()) { // check out each task...
                try {
                    Future<TestResult> future = iterator.next();
                    if (future.isDone()) { // task is over!
                        iterator.remove(); // remove from the list of TODOs
                        TestResult testResultObtained = future.get(); // obtain result
                        testResults.add(testResultObtained); // add test result to the list (test is done)
                        LOG.log(Level.INFO, "[{0}] {1} ({2}ms)", new Object[]{
                            testResultObtained.isPass() ? "PASS" : "FAIL", testResultObtained.getTestName(), testResultObtained.getDuration()
                        });                        
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    LOG.log(Level.SEVERE, "Incredible exception", ex);
                }
            }
        }

        return testResults;
    }

    @Schedule(hour = "*", minute = "*/2", second = "0", info = "TestRunner", persistent = false)
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
