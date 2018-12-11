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
package org.jaqpot.core.service.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author pantelispanka
 */
@Provider
public class CORSRequestFilter implements ContainerRequestFilter{
    
    private static final Logger LOG = Logger.getLogger(CORSRequestFilter.class.getName());
    /**
     * Replies with Status 200 to all OPTION Requests.
     *
     * @param request
     * @throws IOException
     */
    @Override
    public void filter(ContainerRequestContext request) throws IOException {
//        String userName = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "unkown";
//        LOG.log(Level.INFO, "Request: {0} {1} by User {2}", new Object[]{request.getMethod(), request.getUriInfo().getRequestUri(), userName});

        LOG.log(Level.FINE, "Executing  CORS Request Filter");
        if (request.getRequest().getMethod().equals("OPTIONS")) {
            LOG.log(Level.FINE, "HTTP Method (OPTIONS) - Detected!");
            request.abortWith(Response.status(Response.Status.OK).build());
        }
    }
}
