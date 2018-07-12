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
package org.jaqpot.core.service.authenitcation;

import com.fasterxml.jackson.databind.util.Annotations;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.management.relation.Role;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.User;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

/**
 *
 * @author pantelispanka
 */
@TokenSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
@Produces("application/json")
public class TokenRequestFilter implements ContainerRequestFilter, Serializable {

    @Context
    private ResourceInfo resourceInfo;

    @EJB
    AAService aaService;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        Method resourceMethod = resourceInfo.getResourceMethod();
//        List<RoleEnum> classRoles = extractRoles(resourceMethod);

        MultivaluedMap headers = requestContext.getHeaders();
        String api_key;
        try {
            String authorizationHeader
                    = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String[] apiA = authorizationHeader.split("\\s+");

            api_key = apiA[1];
//            api_key = api_key.substring(1, api_key.length() - 1);
            aaService.validateAccessToken(api_key);
            this.validateRole(api_key, resourceMethod);
            this.handleSecurityContext(api_key, requestContext);

        } catch (NullPointerException e) {
            ErrorReport error = new ErrorReport();
            error.setCode("Authorization header not provided. Please provide header and token");
            error.setMessage("Authorization header not provided. Please provide header and token");
            error.setHttpStatus(401);
            error.setDetails(Arrays.toString(e.getStackTrace()));
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED).entity(error)
                    .build());
        } catch (JaqpotNotAuthorizedException e) {
            ErrorReport error = e.getError();
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED).entity(error)
                    .build());
        }

    }

    public List<RoleEnum> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<>();
        } else {
            TokenSecured secured = annotatedElement.getAnnotation(TokenSecured.class);
//            Annotation[] an = annotatedElement.getAnnotations();
            if (secured == null) {
                return new ArrayList<>();
            } else {
                RoleEnum[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }

    public void validateRole(String api_key, Method resourceMethod) throws JaqpotNotAuthorizedException {
        List<RoleEnum> role = this.extractRoles(resourceMethod);
        if (role.contains(RoleEnum.ADMNISTRATOR) && !aaService.isAdmin(api_key)) {
            throw new JaqpotNotAuthorizedException("Only admins have access");
        }
    }

    private void handleSecurityContext(String api_key, ContainerRequestContext requestContext) throws JaqpotNotAuthorizedException {
        User user = aaService.getUserFromSSO(api_key);
        Principal userPrincipal = new UserPrincipal(user.getId());
        SecurityContext securityContext = new SecurityContextImpl(userPrincipal);
        requestContext.setSecurityContext(securityContext);
    }

}
