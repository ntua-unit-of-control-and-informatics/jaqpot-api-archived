///*
// *
// * JAQPOT Quattro
// *
// * JAQPOT Quattro and the components shipped with it (web applications and beans)
// * are licensed by GPL v3 as specified hereafter. Additional components may ship
// * with some other licence as will be specified therein.
// *
// * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// * Source code:
// * The source code of JAQPOT Quattro is available on github at:
// * https://github.com/KinkyDesign/JaqpotQuattro
// * All source files of JAQPOT Quattro that are stored on github are licensed
// * with the aforementioned licence.
// */
//package org.jaqpot.core.service.filter;
//
//import org.jaqpot.core.data.UserHandler;
//import org.jaqpot.core.model.User;
//import org.jaqpot.core.model.factory.ErrorReportFactory;
//import org.jaqpot.core.model.factory.UserFactory;
//import org.jaqpot.core.properties.PropertyManager;
//import org.jaqpot.core.service.annotations.Authorize;
//import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
//
//import javax.annotation.Priority;
//import javax.ejb.EJB;
//import javax.inject.Inject;
//import javax.ws.rs.Priorities;
//import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.ContainerRequestFilter;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.SecurityContext;
//import javax.ws.rs.ext.Provider;
//import java.io.IOException;
//import java.lang.annotation.Annotation;
//import java.lang.reflect.AnnotatedElement;
//import java.lang.reflect.Method;
//import java.security.Principal;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.ws.rs.Path;
//import javax.ws.rs.container.ResourceInfo;
//import javax.ws.rs.core.Context;
//
///**
// *
// * @author Pantelis Sopasakis
// * @author Charalampos Chomenidis
// *
// */
//@Provider
//@Authorize
//@Priority(Priorities.AUTHENTICATION)
//public class AuthorizationRequestFilter implements ContainerRequestFilter {
//
//
//    @Inject
//    PropertyManager propertyManager;
//
//    @EJB
//    UserHandler userHandler;
//    
//    @Context
//    private ResourceInfo resourceInfo;
//
//    private static final Logger LOG = Logger.getLogger(AuthorizationRequestFilter.class.getName());
//
//    public AuthorizationRequestFilter() {
//    }
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) throws IOException {
//
//        AnnotatedElement resourceClass = resourceInfo.getResourceClass();
//        Path classResource = resourceClass.getAnnotation(Path.class);
//        
//        AnnotatedElement resourceMethod = resourceInfo.getResourceMethod();
//        AuthorizationEnum[] authAnnot = resourceMethod.getAnnotation(Authorize.class).value();
//
//        String resource = classResource.value();
//        
//        System.out.println(resource);
//        
//
//        
//
//
//    }
//
//}
