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
package org.jaqpot.core.model.ld;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Charalampos Chomenidis
 * @author Angelos Valsamis
 * 
 */
public abstract class BaseLD {

    @JsonProperty(value = "@context")
    protected Map<String, Object> context;

    protected String id;

    protected String type;

    private List<String> title;

    private List<String> description;

    private List<String> subject;

    private List<String> publisher;

    private List<String> creator;

    private List<String> contributor;

    private List<String> audience;

    private List<String> rights;

    private List<String> source;

    private List<String> sameAs;

    private List<String> seeAlso;

    private List<String> comment;

    private String date;

    private String doi;

    public BaseLD() {
        context = new HashMap<>();
        context.put("id", "@id");
        context.put("type", "@type");
        context.put("enm", "http://purl.enanomapper.org/onto/");
        context.put("dc", "http://purl.org/dc/terms/");
        context.put("bibo", "http://purl.org/ontology/bibo/doi");
        context.put("owl", "http://www.w3.org/2002/07/owl#");
        context.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        context.put("ot", "http://www.opentox.org/api/1.1#");
        context.put("title", "dc:title");
        context.put("description", "dc:description");
        context.put("subject", "dc:subject");
        context.put("publisher", "dc:publisher");
        context.put("creator", "dc:creator");
        context.put("contributor", "dc:contributor");
        context.put("audience", "dc:audience");
        context.put("rights", "dc:rights");
        context.put("source", "dc:source");
        context.put("date", "dc:date");
        context.put("doi", "bibo:doi");
        context.put("sameAs", "owl:sameAs");
        context.put("seeAlso", "rdfs:seeAlso");
        context.put("comment", "rdfs:comment");
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTitle() {
        return title;
    }

    public void setTitle(List<String> title) {
        this.title = title;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    public List<String> getPublisher() {
        return publisher;
    }

    public void setPublisher(List<String> publisher) {
        this.publisher = publisher;
    }

    public List<String> getCreator() {
        return creator;
    }

    public void setCreator(List<String> creator) {
        this.creator = creator;
    }

    public List<String> getContributor() {
        return contributor;
    }

    public void setContributor(List<String> contributor) {
        this.contributor = contributor;
    }

    public List<String> getAudience() {
        return audience;
    }

    public void setAudience(List<String> audience) {
        this.audience = audience;
    }

    public List<String> getRights() {
        return rights;
    }

    public void setRights(List<String> rights) {
        this.rights = rights;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public List<String> getSameAs() {
        return sameAs;
    }

    public void setSameAs(List<String> sameAs) {
        this.sameAs = sameAs;
    }

    public List<String> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(List<String> seeAlso) {
        this.seeAlso = seeAlso;
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }

}
