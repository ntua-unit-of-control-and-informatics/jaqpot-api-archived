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
package org.jaqpot.core.elastic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonGeneratorImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.builder.BibTeXBuilder;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.builder.UserBuilder;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.util.XmlNameExtractor;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 * @param <T>
 */
public class ElasticSearchWriter<T extends JaqpotEntity> {

    private final T entity;
    private ObjectMapper om; // this must be singleton!
    private Client client; // this must be singleton!

    public ElasticSearchWriter(T entity) {
        this.entity = entity;
    }

    

    /**
     * Indexes an entity and returns the ID of the stored entity index. Indexes
     * are stored under /{index_name}/{type_name}/{id}, where {index_name} is
     * retrieved from the name of the POJO (as specified by the #name parameter
     * of XmlRootElement) with an `s` in the end. For example, the {index_name}
     * of {@link User} is <code>users</code>.
     *
     * @return
     */
    public String index() {
        try {
            System.out.println(entity.getClass().getCanonicalName());
            IndexResponse response;
            IndexRequestBuilder requestBuilder = client.prepareIndex();
            requestBuilder = requestBuilder.setIndex("jaqpot").setType(
                    XmlNameExtractor.extractName(entity.getClass()));
            if (entity.getId() == null) {
                requestBuilder = requestBuilder.setCreate(true);
            } else {
                requestBuilder = requestBuilder.setId(entity.getId());
            }

            String jsonObject = om.writeValueAsString(entity);            
            System.out.println(jsonObject);
            response = requestBuilder
                    .setSource(jsonObject)
                    .execute()
                    .actionGet();
            return response.getId();
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ElasticSearchWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Jackson failed to serialize this entity!");
        }
    }

    public static void main(String... args) throws IOException {
        User u = UserBuilder.builder("jack.sparrow").
                setHashedPassword(UUID.randomUUID().toString()).
                setMail("jack.sparrow@gmail.com").
                setMaxBibTeX(17680).
                setMaxFeatures(12678).
                setMaxModels(9999).
                setName("Jack Sparrow").
                setMaxSubstances(1000)
                .setMaxWeeklyPublishedFeatures(345)
                .setMaxWeeklyPublishedModels(67)
                .setMaxWeeklyPublishedSubstances(5)
                .setMaxParallelTasks(10).build();
        
        //u.setId(null);

        BibTeX b = BibTeXBuilder.builder("WhaTevEr14")
                .setAbstract("asdf")
                .setAddress("sdfsdfsd")
                .setAnnotation("slkfsdlf")
                .setAuthor("a")
                .setBibType(BibTeX.BibTYPE.Article)
                .setBookTitle("t6hdth")
                .setChapter("uthjsdfbkjs")
                .setCopyright("sdfsdf")
                .setCrossref("other")
                .setEdition("234234")
                .setEditor("me")
                .setISBN("23434234")
                .setISSN("10010231230")
                .setJournal("haha")
                .setKey("lalala")
                .setKeywords("This is a set of keywords")
                .setNumber("1")
                .setPages("101-123")
                .setSeries("Some series")
                .setTitle("Lololo")
                .setURL("http://some.url.ch/")
                .setVolume("100")
                .setYear("2010")
                .build();

        ROG rog = new ROG(false);
        Model m = rog.nextModel();
        m = new ModelMetaStripper(m).strip();
        
        
        Feature f = new Feature();
        f.setId("456");
        Set<String> ontClasses = new HashSet<>();
        ontClasses.add("ot:Feature");
        ontClasses.add("ot:NumericFeature");
        f.setOntologicalClasses(ontClasses);
        f.setMeta(MetaInfoBuilder.builder()
                .addComments("this is a comment", "and a second one")
                .addTitles("My first feature", "a nice feature")
                .addSubjects("feature of the day")
                .build());
        f.setUnits("mJ");                
                        
        
        ElasticSearchWriter writer = new ElasticSearchWriter(b);
        InetSocketTransportAddress addrOpenTox=new InetSocketTransportAddress("147.102.82.32", 49101);
        InetSocketTransportAddress addrLocal=new InetSocketTransportAddress("localhost", 9300);
        writer.client = new TransportClient()                
                //.addTransportAddress(addrLocal)
                .addTransportAddress(addrOpenTox);
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        writer.om = om;
        System.out.println("http://147.102.82.32:49234/jaqpot/"+
                XmlNameExtractor.extractName(writer.entity.getClass())+"/"+writer.index());
    }

}
