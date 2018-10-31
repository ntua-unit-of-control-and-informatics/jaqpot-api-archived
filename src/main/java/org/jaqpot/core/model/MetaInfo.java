/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
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

import java.util.Date;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaInfo {

    private Set<String> identifiers;
    private List<String> comments;
    private Set<String> descriptions;
    private Set<String> titles;
    private Set<String> subjects;
    private Set<String> publishers;
    private Set<String> creators;
    private Set<String> contributors;
    private Set<String> audiences;
    private Set<String> rights;
    private Set<String> sameAs;
    private Set<String> seeAlso;
    private Set<String> hasSources;
    private Set<String> doi;
    private Date date;
    private String picture;
    private String markdown;
    private Set<String> tags;
    private boolean locked;

    public MetaInfo(MetaInfo other) {
        if (other == null) {
            throw new NullPointerException("You cannot clone a null MetaInfo object");
        }
        this.audiences = other.audiences != null ? new HashSet<>(other.audiences) : null;
        this.comments = other.comments != null ? new ArrayList<>(other.comments) : null;
        this.contributors = other.contributors != null ? new HashSet<>(other.contributors) : null;
        this.creators = other.creators != null ? new HashSet<>(other.creators) : null;
        this.descriptions = other.descriptions != null ? new HashSet<>(other.descriptions) : null;
        this.hasSources = other.hasSources != null ? new HashSet<>(other.hasSources) : null;
        this.identifiers = other.identifiers != null ? new HashSet<>(other.identifiers) : null;
        this.publishers = other.publishers != null ? new HashSet<>(other.publishers) : null;
        this.rights = other.rights != null ? new HashSet<>(other.rights) : null;
        this.sameAs = other.sameAs != null ? new HashSet<>(other.sameAs) : null;
        this.seeAlso = other.seeAlso != null ? new HashSet<>(other.seeAlso) : null;
        this.subjects = other.subjects != null ? new HashSet<>(other.subjects) : null;
        this.titles = other.titles != null ? new HashSet<>(other.titles) : null;
        this.date = other.date != null ? (Date) other.date.clone() : null;
        this.doi = other.doi != null ? new HashSet<>(other.doi) : null;
        this.locked=other.locked;
    }

    public MetaInfo() {}

    public Set<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public Set<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Set<String> descriptions) {
        this.descriptions = descriptions;
    }

    public Set<String> getTitles() {
        return titles;
    }

    public void setTitles(Set<String> titles) {
        this.titles = titles;
    }

    public Set<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<String> subjects) {
        this.subjects = subjects;
    }

    public Set<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Set<String> publishers) {
        this.publishers = publishers;
    }

    public Set<String> getCreators() {
        return creators;
    }

    public void setCreators(Set<String> creators) {
        this.creators = creators;
    }

    public Set<String> getContributors() {
        return contributors;
    }

    public void setContributors(Set<String> contributors) {
        this.contributors = contributors;
    }

    public Set<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(Set<String> audiences) {
        this.audiences = audiences;
    }

    public Set<String> getRights() {
        return rights;
    }

    public void setRights(Set<String> rights) {
        this.rights = rights;
    }

    public Set<String> getSameAs() {
        return sameAs;
    }

    public void setSameAs(Set<String> sameAs) {
        this.sameAs = sameAs;
    }

    public Set<String> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(Set<String> seeAlso) {
        this.seeAlso = seeAlso;
    }

    public Set<String> getHasSources() {
        return hasSources;
    }

    public void setHasSources(Set<String> hasSources) {
        this.hasSources = hasSources;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<String> getDoi() {
        return doi;
    }

    public void setDoi(Set<String> doi) {
        this.doi = doi;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }
    
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    
}
