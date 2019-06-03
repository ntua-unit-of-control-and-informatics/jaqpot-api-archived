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

//import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import org.jaqpot.core.properties.PropertyManager;
import org.reflections.Reflections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.jaqpot.core.service.authentication.TokenRequestFilter;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@ApplicationPath("/services")
@Singleton
@Startup
public class JaqpotRestApplication extends Application {

    private static final Logger LOG = Logger.getLogger(JaqpotRestApplication.class.getName());

    //BeanConfig beanConfig;
    @Inject
    PropertyManager propertyManager;

    public JaqpotRestApplication() {

    }

    //Move constructor logic in @PostConstruct in order to be able to use PropertyManager Injection
    @PostConstruct
    
    public void init() {
        String host = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_HOST);
        String port = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PORT);
        String basePath = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE);
        LOG.log(Level.INFO, "Starting up Jaqpot API");
        LOG.log(Level.INFO, "Host:{0}", host);
        LOG.log(Level.INFO, "Port:{0}", port);
        LOG.log(Level.INFO, "BasePath:{0}", basePath);
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("Jaqpot Quattro")
                .description("Jaqpot Quattro")
                .version("4.0.?");
        oas.openapi("3.0.1");
        oas.info(info);

        SecurityScheme securityScheme = new SecurityScheme();
        //ApiKeyAuthDefinition apiKeyDefinition = new ApiKeyAuthDefinition();
        securityScheme.setName("HTTP");
        securityScheme.setType(SecurityScheme.Type.HTTP);
        SecurityRequirement sr = new SecurityRequirement();
        sr.addList("HTTP");
        oas.addSecurityItem(sr);
        Server server = new Server().url(host + ":" + port + basePath);
        List<Server> servers = new ArrayList();
        servers.add(server);
        oas.servers(servers);
        

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                //flag
                .openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(Stream.of("org.jaqpot.core.service.resource").collect(Collectors.toSet()));

        try {
            OpenAPI openapi = new JaxrsOpenApiContextBuilder()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
           
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        /*beanConfig = new BeanConfig();
         beanConfig.setVersion("4.0.3");
         beanConfig.setResourcePackage("org.jaqpot.core.service.resource");
         beanConfig.setScan(true);
         beanConfig.setTitle("Jaqpot Quattro");
         beanConfig.setDescription("Jaqpot Quattro");
         if(!"80".equals(port)){
         beanConfig.setHost(host + ":" + port);
         }
         else{
         beanConfig.setHost(host);
         }
         beanConfig.setBasePath(basePath);
         beanConfig.setSchemes(new String[]{"http","https"});
         beanConfig.setPrettyPrint(true);
         */
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

        resources.add(TokenRequestFilter.class);
        // Swagger-related stuff [Registered directly]
        //resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        //resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        resources.add(OpenApiResource.class);
        resources.add(AcceptHeaderOpenApiResource.class);

        return resources;
    }

}
