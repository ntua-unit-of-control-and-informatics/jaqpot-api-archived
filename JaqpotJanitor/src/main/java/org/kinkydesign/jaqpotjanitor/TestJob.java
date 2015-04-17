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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kinkydesign.jaqpotjanitor.core.AssertionException;
import org.kinkydesign.jaqpotjanitor.core.TestResult;

/**
 *
 * @author chung
 */
public class TestJob implements Callable<TestResult> {

    private static final Logger LOG = Logger.getLogger(TestJob.class.getName());

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
     * Returns the timestamp when the job starts running. If the job has not
     * started yet, this method returns <code>null</code>.
     *
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
        try {
            m.invoke(t);
            tr.setMessage("Test has succeeded");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            tr.setPass(false);
            tr.setStackTrace(exceptionAsString);
            tr.setMessage(cause.getMessage());
            if (!(cause instanceof AssertionException)) {
                LOG.log(Level.SEVERE, null, cause);
            }
        }
        tr.setDuration(System.currentTimeMillis() - started);
        tr.setTimestamp(started);
        return tr;
    }
}
