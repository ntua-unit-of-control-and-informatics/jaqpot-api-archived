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
package org.jaqpot.core.sessions;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.BadRequestException;
import org.jaqpot.core.messagebeans.SearchSessionConsumer;
import org.jaqpot.core.model.dto.search.FountEntities;
import org.jaqpot.core.properties.PropertyManager;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 *
 * @author pantelispanka
 */
@Startup
@DependsOn("OnAppInit")
@Singleton
public class SessionClient {

    static final Logger LOGGER = Logger.getLogger(SessionClient.class.getName());

    @EJB
    PropertyManager pm;

    private JedisCluster jedisCluster;
    private Jedis jedis;

    @PostConstruct
    public void init() {

        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_EXISTS).equals("true") && pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_ON_CLUSTER).equals("true")) {
            LOGGER.log(Level.INFO, "Staring Redis on cluster");
            try {
                Set<HostAndPort> connectionPoints = new HashSet<>();
                String[] hosts = pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_CLUSTER).split(",");
                LOGGER.log(Level.INFO, Arrays.toString(hosts));
                LOGGER.log(Level.INFO, pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_CLUSTER));
                for (String host : hosts) {
                    LOGGER.log(Level.INFO, host);
                    String[] hostAndPort = host.split(":");
                    int port = Integer.parseInt(hostAndPort[1]);
                    String hostIp = hostAndPort[0];
                    connectionPoints.add(new HostAndPort(hostIp, port));
                }
                this.jedisCluster = new JedisCluster(connectionPoints);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            }
        } else if (pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_EXISTS).equals("true")) {
            try {
                LOGGER.log(Level.INFO, "Staring Redis simple");
                this.jedis = new Jedis(pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_DB));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
    }

    public Jedis getJedis() {
        return this.jedis;
    }

    public JedisCluster getJedisCluster() {
        return this.jedisCluster;
    }

    public void searchSession(String sessionId, List<String> entityIds, Boolean finished) {
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_ON_CLUSTER).equals("true")) {
            String finishedKey = sessionId + "_finished";
            String foundKey = sessionId + "_found";
            entityIds.forEach(eid -> {
                this.jedisCluster.rpush(foundKey, eid);
            });
            this.jedisCluster.set(finishedKey, finished.toString());
            this.jedisCluster.expire(finishedKey, 480);
            this.jedisCluster.expire(foundKey, 480);
        } else {
            String finishedKey = sessionId + "_finished";
            String foundKey = sessionId + "_found";
            entityIds.forEach(eid -> {
                this.jedis.rpush(foundKey, eid);
            });
            this.jedis.set(finishedKey, finished.toString());
            this.jedis.expire(finishedKey, 480);
            this.jedis.expire(foundKey, 480);
        }
    }

    public void setSearchDurration(String sessionId, long duration) {
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_ON_CLUSTER).equals("true")) {
            String searchDuration = sessionId + "_duration";
            this.jedisCluster.set(searchDuration, Long.toString(duration));
            this.jedisCluster.expire(searchDuration, 480);
        } else {
            String searchDuration = sessionId + "_duration";
            this.jedis.set(searchDuration, Long.toString(duration));
            this.jedis.expire(searchDuration, 480);
        }
    }

    public Boolean searchSessionExcists(String sessionId) {
        Boolean exists = null;
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_ON_CLUSTER).equals("true")) {
            exists = this.jedisCluster.exists(sessionId + "_finished");
        } else {
            exists = this.jedis.exists(sessionId + "_finished");
        }
        return exists;
    }

    public FountEntities searchSessionFound(String session, Integer from, Integer to) {

        FountEntities fe = new FountEntities();
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.REDIS_ON_CLUSTER).equals("true")) {
            try {
                String finished = this.jedisCluster.get(session + "_finished");
                fe.setFinished(finished);
                if (finished.equals("true")) {
                    fe.setDuration(this.jedisCluster.get(session + "_duration"));
                    fe.setTotal(this.jedisCluster.llen(session + "_found"));
                }
                List<String> fount = this.jedisCluster.lrange(session + "_found", from, to);
                fe.setEntityId(fount);
            } catch (NullPointerException ne) {
                throw new BadRequestException("Search request is by long gone. Please search again!");
            }

        } else {

            try {
                String finished = this.jedis.get(session + "_finished");
                fe.setFinished(finished);
                if (finished.equals("true")) {
                    fe.setDuration(this.jedis.get(session + "_duration"));
                    fe.setTotal(this.jedis.llen(session + "_found"));
                }
                List<String> fount = this.jedis.lrange(session + "_found", from, to);
                fe.setEntityId(fount);
            } catch (NullPointerException ne) {
                throw new BadRequestException("Search request is by long gone. Please search again!");
            }
        }
        return fe;
    }

    @PreDestroy
    public void destroy() {
        if (this.jedis != null) {
            this.jedis.close();
        }
        if (this.jedisCluster != null) {
            try {
                this.jedisCluster.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            }

        }
    }

}
