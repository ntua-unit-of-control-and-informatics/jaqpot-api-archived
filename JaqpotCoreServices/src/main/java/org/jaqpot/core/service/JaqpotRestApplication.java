/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.jaqpot.core.service.writer.UriBodyWriter;
import org.jaqpot.core.service.writer.UriListBodyWriter;
import org.jaqpot.core.service.resource.AlgorithmResource;
import org.jaqpot.core.service.resource.TaskResource;

/**
 *
 * @author hampos
 */
@ApplicationPath("/services")
public class JaqpotRestApplication extends Application {

    public JaqpotRestApplication() {        
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath("http://localhost:8080/jaqpot/services");
        beanConfig.setResourcePackage("org.jaqpot.core.service.resource");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet();

        resources.add(AlgorithmResource.class);
        resources.add(TaskResource.class);

        resources.add(JacksonJaxbJsonProvider.class);
        resources.add(UriListBodyWriter.class);
        resources.add(UriBodyWriter.class);

        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);

        return resources;
    }
}
