/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import org.reflections.Reflections;


/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@ApplicationPath("/services")
public class JaqpotRestApplication extends Application {

    public JaqpotRestApplication() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath("http://localhost:8080/jaqpot/services");
        beanConfig.setResourcePackage("org.jaqpot.core.service.resource");
        beanConfig.setScan(true);
        beanConfig.setTitle("Jaqpot Quattro");
        beanConfig.setDescription("Jaqpot Quattro");        
        
    }

    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet();
        
        /*
         * We are here using reflections to discover and register
         * resources, filters and providers. 
         */

        // Resources [Annotated with @Path]
        Reflections reflectedResources = new Reflections("org.jaqpot.core.service.resource");
        resources.addAll(reflectedResources.getTypesAnnotatedWith(javax.ws.rs.Path.class));
                                
        // Various providers [Annotated with @Provider]
        Reflections reflectedProviders = new Reflections("org.jaqpot.core.service.filter");
        resources.addAll(reflectedProviders.getTypesAnnotatedWith(Provider.class));
        
        // Various providers [Annotated with @Provider]
        Reflections reflectedExceptionMappers = new Reflections("org.jaqpot.core.service.filter");
        resources.addAll(reflectedExceptionMappers.getTypesAnnotatedWith(Provider.class));
                        
        // Writers [Annotated with @Provider]
        Reflections reflectedWriters = new Reflections("org.jaqpot.core.service.writer");
        resources.addAll(reflectedWriters.getTypesAnnotatedWith(Provider.class));

        // Swagger-related stuff [Registered directly]
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);

        return resources;
    }

}
