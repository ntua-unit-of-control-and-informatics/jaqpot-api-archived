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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class JanitorScheduledJobTest {

    public JanitorScheduledJobTest() {
    }

    class Job implements Callable<String> {

        private volatile Long started = null;

        public Long getStarted() {
            return started;
        }
             
        
        @Override
        public String call() throws Exception {           
            started = System.currentTimeMillis();            
            while (System.currentTimeMillis() - started < 10000) { /* wait here! */}
            System.out.println("OOPS!");
            return "OK";
        }
                
    }

    @Test
    public void test() throws Exception {
       
        ExecutorService exe = Executors.newFixedThreadPool(2);
        Job j = new Job();
        Future<String> future = exe.submit(j);
        
        while (j.getStarted()==null){ /* wait until it starts */ }

        assertTrue(future.cancel(true)); // cancel                      
        
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 10000) { /* wait here! */}
    }

}
