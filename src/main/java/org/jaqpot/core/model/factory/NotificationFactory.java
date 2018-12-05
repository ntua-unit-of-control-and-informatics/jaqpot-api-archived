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
package org.jaqpot.core.model.factory;

import org.jaqpot.core.model.Notification;
import org.jaqpot.core.model.Notification.Type;
import org.jaqpot.core.model.builder.NotificationBuilder;

/**
 *
 * @author pantelispanka
 */
public class NotificationFactory {
    
//    public static Notification invitationNotification(){
//        return invitationNotification();
//    }
    
    
    public static Notification invitationNotification(String from, String to, String initationToOrg){
        String body = "Invitation to become a member of the Organization " + initationToOrg;
        Notification notif = NotificationBuilder
                .builderRandomId()
                .setFrom(from)
                .setTo(to)
                .setOwner(to)
                .setBody(body)
                .setTime(Notification.Time.NEW)
                .setType(Type.INVITATION)
                .setViewed(Boolean.FALSE)
                .setInvitationTo(initationToOrg)
                .build();
        return notif;
    }
    
    public static Notification modelShareNotification(String from, String to, String modelId){
        String body = "A model just been shared with you " + modelId;
        Notification notif = NotificationBuilder
                .builderRandomId()
                .setFrom(from)
                .setTo(to)
                .setOwner(to)
                .setBody(body)
                .setTime(Notification.Time.NEW)
                .setType(Type.SHARE)
                .setViewed(Boolean.FALSE)
                .setEntityShared(modelId)
                .build();
        return notif;
    }
    
    public static Notification datasetShareNotification(String from, String to, String datasetId){
        String body = "A Dataset just been shared with you " + datasetId;
        Notification notif = NotificationBuilder
                .builderRandomId()
                .setFrom(from)
                .setTo(to)
                .setOwner(to)
                .setBody(body)
                .setTime(Notification.Time.NEW)
                .setType(Type.SHARE)
                .setViewed(Boolean.FALSE)
                .setEntityShared(datasetId)
                .build();
        return notif;
    }
    
    
    public static Notification randomIdNotification(){
        Notification notif = NotificationBuilder
                .builderRandomId().build();
        return notif;
    }
    
}
