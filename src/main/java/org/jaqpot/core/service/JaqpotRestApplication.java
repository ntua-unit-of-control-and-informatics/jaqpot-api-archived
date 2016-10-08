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
package org.jaqpot.core.service;

import io.swagger.jaxrs.config.BeanConfig;
import org.jaqpot.core.properties.PropertyManager;
import org.reflections.Reflections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@ApplicationPath("/services")
public class JaqpotRestApplication extends Application {
    BeanConfig beanConfig;

    @Inject
    PropertyManager propertyManager;

    public JaqpotRestApplication() {
        beanConfig = new BeanConfig();
        beanConfig.setVersion("1.5.0");
        beanConfig.setResourcePackage("org.jaqpot.core.service.resource");
        beanConfig.setScan(true);
        beanConfig.setTitle("Jaqpot Quattro");
        beanConfig.setDescription("Jaqpot Quattro");
    }

    //Move constructor logic in @PostConstruct in order to be able to use PropertyManager Injection
    @PostConstruct
    public void init() {
        beanConfig.setBasePath(propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE));

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
        Reflections reflectedExceptionMappers = new Reflections("org.jaqpot.core.service.filter.excmappers");
        resources.addAll(reflectedExceptionMappers.getTypesAnnotatedWith(Provider.class));

        // Writers [Annotated with @Provider]
        Reflections reflectedWriters = new Reflections("org.jaqpot.core.service.writer");
        resources.addAll(reflectedWriters.getTypesAnnotatedWith(Provider.class));

        // Swagger-related stuff [Registered directly]
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);        

        return resources;
    }

}
