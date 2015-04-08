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
package org.jaqpot.core.service.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.factory.UserFactory;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.security.SecurityContextImpl;
import org.jaqpot.core.service.security.UserPrincipal;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Provider
@Authorize
public class AuthorizationRequestFilter implements ContainerRequestFilter {

    @EJB
    AAService aaService;

    private ResourceBundle configResourceBundle;

    @EJB
    UserHandler userHandler;

    private static final Logger LOG = Logger.getLogger(AuthorizationRequestFilter.class.getName());

    @PostConstruct
    private void init() {
        configResourceBundle = ResourceBundle.getBundle("config");
    }

    private void _handleAnonymous(ContainerRequestContext requestContext) {
        // When operating without AA, all users are anonymous
        Principal userPrincipal = new UserPrincipal("anonymous");
        SecurityContext securityContext = new SecurityContextImpl(userPrincipal);
        requestContext.setSecurityContext(securityContext);
        User anonymousUser = userHandler.find("anonymous");
        if (anonymousUser == null) { // The anonymous user is a DB entry
            // create an anonymous user if it doesn't exist!
            anonymousUser = UserFactory.newNormalUser("anonymous", "anonymous");
            anonymousUser.setName("Anonymous User");
            anonymousUser.setMail("anonymous@jaqpot.org");
            userHandler.create(anonymousUser);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // when AA is not enabled...
        if ("false".equals(configResourceBundle.getString("jaqpot.aa"))) {
            _handleAnonymous(requestContext);
            return;
        }

        // Check whether there is an AA token...
        String token = requestContext.getHeaderString("subjectid");
        if (token == null) {
            requestContext.abortWith(Response
                    .ok(ErrorReportFactory.unauthorized("Please provide an authorization token in a subjectid header."))
                    .status(Response.Status.UNAUTHORIZED)
                    .build());
        }

        // is the token valid? if not: forbidden...
        if (!aaService.validate(token)) {
            requestContext.abortWith(Response.
                    ok(ErrorReportFactory.unauthorized("Your authorization token is not valid."))
                    .status(Response.Status.FORBIDDEN)
                    .build());
            return;
        }

        // who is this user? is the user cached?
        User user = aaService.getUserFromToken(token);
        if (user != null) {
            return; // user is cached!
        }

        // the token is valid - ask SSO who this user is...
        // the user is not cached...
        user = aaService.getUserFromSSO(token);
        aaService.registerUserToken(token, user); // cache the user

        LOG.log(Level.INFO, "New user on Jaqpot with ID {0} and name {1}", 
                new Object[]{user.getId(), user.getName()});

        // is the user in the DB?
        User userInDB = userHandler.find(user.getId());
        if (userInDB == null) { // user not in DB - create...
            LOG.log(Level.INFO, "New user registered in DB with ID {0}", user.getId());
            userHandler.create(user);
        }

    }

}
