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
package org.jaqpot.core.service.filter;

import java.io.IOException;
import java.security.Principal;
import javax.ejb.EJB;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
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

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getRequestUri().getPath();
        if (path.contains("api-docs") || path.contains("login")) {
            return;
        }
        String token = requestContext.getHeaderString("subjectid");
        if (token == null) {
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Please provide an authorization token in a subjectid header.")
                    .build());
        }
        String user = aaService.getUserFromToken(token);
        if (!aaService.validate(token)) {
            if (user != null) {
                aaService.removeToken(token);
            }
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Your authorization token is not valid.")
                    .build());
        }

        if (user != null) {
            Principal userPrincipal = new UserPrincipal(user);
            SecurityContext securityContext = new SecurityContextImpl(userPrincipal);
            requestContext.setSecurityContext(securityContext);
        } else {
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Please login first!")
                    .build());
        }
    }

}
