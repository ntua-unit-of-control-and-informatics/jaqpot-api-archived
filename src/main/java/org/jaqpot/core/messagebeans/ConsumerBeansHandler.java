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
package org.jaqpot.core.messagebeans;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.ws.rs.InternalServerErrorException;
import org.jaqpot.core.properties.PropertyManager;

/**
 *
 * @author pantelispanka
 */
@Singleton
@Startup
@DependsOn("OnAppInit")
public class ConsumerBeansHandler {

    private static final Logger LOG = Logger.getLogger(ConsumerBeansHandler.class.getName());

    @EJB
    IndexEntityConsumer kIndCon;

    @EJB
    SearchSessionConsumer sSesCon;

    @EJB
    DeleteIndexedEntityConsumer diep;

    @EJB
    PropertyManager pm;

    private final int poolSize = 2;
    private final int batchSize = poolSize * 1;
    private final int INTERVAL = 5000;

    private ScheduledExecutorService executor1;

    private ScheduledExecutorService executor2;
    private ScheduledExecutorService executor3;
    private Boolean poll;
    private Future future;

    @Resource
    ManagedExecutorService managedExecutor;

    @PostConstruct
    public void init() {

        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            LOG.log(Level.INFO, "Starting message consumer beans");

            executor1 = Executors.newScheduledThreadPool(poolSize);

            IndexEntityConsumer.indexModelConsumer indexConsumer = kIndCon.new indexModelConsumer();

            Runnable task1 = indexConsumer;

            int initialDelay = 0;
            int period = 1000;
            executor1.scheduleAtFixedRate(task1, initialDelay, period, TimeUnit.MILLISECONDS);

            executor2 = Executors.newScheduledThreadPool(poolSize);

            SearchSessionConsumer.searchSessionConsumer searchSessionConsumer = sSesCon.new searchSessionConsumer();
            Runnable task2 = searchSessionConsumer;
            executor2.scheduleAtFixedRate(task2, initialDelay, period, TimeUnit.MILLISECONDS);

            executor3 = Executors.newScheduledThreadPool(poolSize);

            DeleteIndexedEntityConsumer.deleteIndexedEntityConsumer deleteIndexedEntityConsumer = diep.new deleteIndexedEntityConsumer();
            Runnable task3 = deleteIndexedEntityConsumer;
            executor3.scheduleAtFixedRate(task3, initialDelay, period, TimeUnit.MILLISECONDS);

        } else {
            LOG.log(Level.INFO, "No message consumer beans will start");
        }

    }

    @PreDestroy
    public void shutdown() {

        try {
            executor1.shutdown();
            executor1.awaitTermination(2, TimeUnit.SECONDS);
            executor1 = null;

            executor2.shutdown();
            executor2.awaitTermination(2, TimeUnit.SECONDS);
            executor2 = null;

            executor3.shutdown();
            executor3.awaitTermination(2, TimeUnit.SECONDS);
            executor3 = null;

        } catch (InterruptedException e) {
            throw new InternalServerErrorException("Poller Problem", e);
        }

    }

}
