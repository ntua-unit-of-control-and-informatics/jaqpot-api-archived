/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author chung
 */
public class MetaInfo {

    private static final int HASH_OFFSET = 3, HASH_MOD = 89;

    public MetaInfo() {

    }

    private Set<String> identifiers = new HashSet<>();
    private Set<String> comments = new HashSet<>();
    private Set<String> descriptions = new HashSet<>();
    private Set<String> titles = new HashSet<>();
    private Set<String> subjects = new HashSet<>();
    private Set<String> publishers = new HashSet<>();
    private Set<String> creators = new HashSet<>();
    private Set<String> contributors = new HashSet<>();
    private Set<String> audiences = new HashSet<>();
    private Set<String> rights = new HashSet<>();
    private String date = new Date(System.currentTimeMillis()).toString();
    private Set<String> sameAs = new HashSet<>();
    private Set<String> seeAlso = new HashSet<>();
    private Set<String> hasSources = new HashSet<>();
    private static final long serialVersionUID = 258712452874812L;

    public Set<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<String> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<String> getComments() {
        return comments;
    }

    public void setComments(Set<String> comments) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (identifiers != null) {
            builder.append("identifiers  : ");
            identifiers.stream().map((id) -> {
                builder.append(id);
                return id;
            }).forEach((_item) -> {
                builder.append(" ");
            });
            builder.append("\n");
        }
        return new String(builder);
    }
    
    
    public MetaInfo addDescription(String... description) {
        if (getDescriptions() != null) {
            getDescriptions().addAll(Arrays.asList(description));

        } else {
            HashSet<String> values = new HashSet<>();
            values.addAll(Arrays.asList(description));
            setDescriptions(values);
        }
        return this;
    }
    
    public MetaInfo addComment(String... comment) {
        if (getComments()!= null) {
            getComments().addAll(Arrays.asList(comment));
        } else {
            HashSet<String> values = new HashSet<>();
            values.addAll(Arrays.asList(comment));
            setComments(values);
        }
        return this;
    }
    
    public MetaInfo addSameAs(String value) {
        if (getSameAs() != null) {
            getSameAs().add(value);
        } else {
            HashSet<String> values = new HashSet<>();
            values.add(value);
            setSameAs(values);
        }
        return this;
    }
    
     
     public MetaInfo addHasSource(String hasSource) {
        if (getHasSources() != null) {
            getHasSources().add(hasSource);
        } else {
            HashSet<String> values = new HashSet<>();
            values.add(hasSource);
            setHasSources(values);
        }
        return this;
    }
    
    
    @Override
    public int hashCode() {
        int hash = HASH_OFFSET;
        hash = HASH_MOD * hash + (this.identifiers != null ? this.identifiers.hashCode() : 0);
        hash = HASH_MOD * hash + (this.comments != null ? this.comments.hashCode() : 0);
        hash = HASH_MOD * hash + (this.descriptions != null ? this.descriptions.hashCode() : 0);
        hash = HASH_MOD * hash + (this.titles != null ? this.titles.hashCode() : 0);
        hash = HASH_MOD * hash + (this.subjects != null ? this.subjects.hashCode() : 0);
        hash = HASH_MOD * hash + (this.publishers != null ? this.publishers.hashCode() : 0);
        hash = HASH_MOD * hash + (this.creators != null ? this.creators.hashCode() : 0);
        hash = HASH_MOD * hash + (this.contributors != null ? this.contributors.hashCode() : 0);
        hash = HASH_MOD * hash + (this.rights != null ? this.rights.hashCode() : 0);
        hash = HASH_MOD * hash + (this.audiences != null ? this.audiences.hashCode() : 0);
        hash = HASH_MOD * hash + (this.date != null ? this.date.hashCode() : 0);
        hash = HASH_MOD * hash + (this.sameAs != null ? this.sameAs.hashCode() : 0);
        hash = HASH_MOD * hash + (this.seeAlso != null ? this.seeAlso.hashCode() : 0);
        hash = HASH_MOD * hash + (this.hasSources != null ? this.hasSources.hashCode() : 0);
        return hash;
    }

     private boolean areSetsEqual(Set set1, Set set2) {
        if (set1 == null ^ set2 == null) {
            return false;
        } else if (set1==null && set2==null) {
            return true;
        }

        int s1 = set1.size();
        int s2 = set2.size();
        if (s1 != s2) {
            return false;
        }
        for (Object o : set1) {
            boolean foundInSet2 = false;
            for (Object o2 : set2) {
                if (o.equals(o2)) {
                    foundInSet2 = true;
                    break;
                }
            }
            if (!foundInSet2) {
                return false;
            }
        }
        return true;
    }
     
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetaInfo other = (MetaInfo) obj;
        if (!areSetsEqual(this.identifiers, other.identifiers)) {
            return false;
        }

        if (!areSetsEqual(this.comments, other.comments)) {
            return false;
        }

        if (!areSetsEqual(this.descriptions, other.descriptions)) {
            return false;
        }

        if (!areSetsEqual(this.titles, other.titles)) {
            return false;
        }

        if (!areSetsEqual(this.subjects, other.subjects)) {
            return false;
        }

        if (!areSetsEqual(this.publishers, other.publishers)) {
            return false;
        }
        if (!areSetsEqual(this.creators, other.creators)) {
            return false;
        }
        if (!areSetsEqual(this.contributors, other.contributors)) {
            return false;
        }
        if (!areSetsEqual(this.audiences, other.audiences)) {
            return false;
        }
        if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
            return false;
        }
        if (!areSetsEqual(this.sameAs, other.sameAs)) {
            return false;
        }
        if (!areSetsEqual(this.seeAlso, other.seeAlso)) {
            return false;
        }
        if (!areSetsEqual(this.hasSources, other.hasSources)) {
            return false;
        }
        return true;
    }

}
