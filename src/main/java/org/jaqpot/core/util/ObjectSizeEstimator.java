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
package org.jaqpot.core.util;

import java.lang.instrument.Instrumentation;
import javax.ejb.Singleton;

/**
 *
 * @author pantelispanka
 */
@Singleton
public class ObjectSizeEstimator {

   private static volatile Instrumentation globalInstrumentation;

   /**
    * Implementation of the overloaded premain method that is first invoked by
    * the JVM during use of instrumentation.
    * 
    * @param agentArgs Agent options provided as a single String.
    * @param inst Handle to instance of Instrumentation provided on command-line.
    */
   public static void premain(final String agentArgs, final Instrumentation inst)
   {
      globalInstrumentation = inst;
   }

   /**
    * Implementation of the overloaded agentmain method that is invoked for
    * accessing instrumentation of an already running JVM.
    * 
    * @param agentArgs Agent options provided as a single String.
    * @param inst Handle to instance of Instrumentation provided on command-line.
    */
   public static void agentmain(String agentArgs, Instrumentation inst)
   {
      globalInstrumentation = inst;
   }

   /**
    * Provide the memory size of the provided object (but not it's components).
    * 
    * @param object Object whose memory size is desired.
    * @return The size of the provided object, not counting its components
    *    (described in Instrumentation.getObjectSize(Object)'s Javadoc as "an
    *    implementation-specific approximation of the amount of storage consumed
    *    by the specified object").
    * @throws IllegalStateException Thrown if my Instrumentation is null.
    */
   public static long getObjectSize(final Object object)
   {
      if (globalInstrumentation == null)
      {
         throw new IllegalStateException("Agent not initialized.");
      }
      return globalInstrumentation.getObjectSize(object);
   }
}
