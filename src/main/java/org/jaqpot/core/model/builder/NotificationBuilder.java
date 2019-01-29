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
package org.jaqpot.core.model.builder;

import java.util.Arrays;
import java.util.HashSet;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Notification;
import org.jaqpot.core.model.Notification.Time;
import org.jaqpot.core.model.Notification.Type;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author pantelispanka
 */
public class NotificationBuilder implements EntityBuilder<Notification> {

    private final Notification notification;

    @Override
    public Notification build() {
        return this.notification;
    }

    public NotificationBuilder() {
        this.notification = new Notification();
    }

    public NotificationBuilder(String id) {
        this.notification = new Notification();
        this.notification.setId(id);
    }

    public static NotificationBuilder builderRandomId() {
        ROG rog = new ROG(true);
        return new NotificationBuilder("NOTIF" + rog.nextString(12));
    }

    public NotificationBuilder setOwner(String owner) {
        this.notification.setOwner(owner);
        return this;
    }

    public NotificationBuilder setFrom(String from) {
        this.notification.setFrom(from);
        return this;
    }

    public NotificationBuilder setTo(String to) {
        this.notification.setTo(to);
        return this;
    }

    public NotificationBuilder setBody(String body) {
        this.notification.setBody(body);
        return this;
    }

    public NotificationBuilder setTime(Time time) {
        this.notification.setTime(time);
        return this;
    }

    public NotificationBuilder setType(Type type) {
        this.notification.setType(type);
        return this;
    }

    public NotificationBuilder setViewed(Boolean viewed) {
        this.notification.setViewed(viewed);
        return this;
    }

    public NotificationBuilder setInvitationTo(String invitationTo) {
        this.notification.setInvitationTo(invitationTo);
        return this;
    }

    public NotificationBuilder setEntityShared(String id){
        this.notification.setEntityShared(id);
        return this;
    }
    
    private void initMeta() {
        if (notification.getMeta() == null) {
            notification.setMeta(new MetaInfo());
        }
    }

    public NotificationBuilder addTitles(String... titles) {
        if (titles == null) {
            return this;
        }
        initMeta();
        if (notification.getMeta().getTitles() == null) {
            notification.getMeta().setTitles(new HashSet<>(titles.length));
        }
        notification.getMeta().getTitles().addAll(Arrays.asList(titles));
        return this;
    }
    
    public NotificationBuilder setBrokenAffiliation(String orgThrough){
        this.notification.setAffiliatedOrg(orgThrough);
        return this;
    }
            

    public NotificationBuilder addDescriptions(String... descriptions) {
        if (descriptions == null) {
            return this;
        }
        initMeta();
        if (notification.getMeta().getDescriptions() == null) {
            notification.getMeta().setDescriptions(new HashSet<>(descriptions.length));
        }
        notification.getMeta().getDescriptions().addAll(Arrays.asList(descriptions));
        return this;
    }

}
