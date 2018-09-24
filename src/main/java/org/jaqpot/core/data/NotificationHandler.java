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
package org.jaqpot.core.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.Notification;

/**
 *
 * @author pantelispanka
 */
@Stateless
public class NotificationHandler extends AbstractHandler<Notification> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public NotificationHandler() {
        super(Notification.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public Long countAllOfOwners(String owner){
        Map<String, Object> properties = new HashMap<>();
        properties.put("owner", owner);
        return em.count(Notification.class, properties);
    }
    
    public List<Notification> getOwnersNotifs(String owner, String query, int start, int end) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("owner", owner);
        if(query.equals("UNREAD")){
            properties.put("viewed", false);
        }
        return em.find(Notification.class, properties, start, end);
    }
    
    public List<Notification> getInvitationsToOrg(String owner, String organization){
         Map<String, Object> properties = new HashMap<>();
        properties.put("owner", owner);
        properties.put("invitationTo", organization);
        return em.find(Notification.class, properties, 0, 100);
    }

}
