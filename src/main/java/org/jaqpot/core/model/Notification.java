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
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author pantelispanka
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notification extends JaqpotEntity {

    public enum Type {
        SIMPLE,
        INVITATION,
        FYI,
        SHARE,
        AFFILIATION,
        BROKENAFFILIATION
    }

    public enum Time {
        OLD,
        NEW
    }

    public enum Answer {
        ACEEPT,
        DECLINE
    }

    private Type type;

    private String body;

    private String from;

    private String to;

    private Boolean viewed;

    private String owner;

    private String invitationTo;
    
    private String entityShared;
    
    private String organizationShared;

    private String affiliatedOrg;

    
    private Boolean resolved;

    private Time time;

    private Answer answer;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewd) {
        this.viewed = viewd;
    }

    public Time getTime() {
        return time;
    }

    public String getEntityShared() {
        return entityShared;
    }

    public void setEntityShared(String entityShared) {
        this.entityShared = entityShared;
    }

    public String getInvitationTo() {
        return invitationTo;
    }

    public void setInvitationTo(String invitationTo) {
        this.invitationTo = invitationTo;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }
    
    public void setTime(Time time) {
        this.time = time;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public String getOrganizationShared() {
        return organizationShared;
    }

    public void setOrganizationShared(String organizationShared) {
        this.organizationShared = organizationShared;
    }

    public String getAffiliatedOrg() {
        return affiliatedOrg;
    }

    public void setAffiliatedOrg(String affiliatedOrg) {
        this.affiliatedOrg = affiliatedOrg;
    }
    
}
