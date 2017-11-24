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

import org.jaqpot.core.properties.PropertyManager;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author chung
 */
@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(CORSResponseFilter.class.getName());

    @Inject
    PropertyManager propertyManager;

    public CORSResponseFilter() {
    }        
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        
        // If the method is OPTIONS return an Allow header and status OK
        if ("OPTIONS".equals(requestContext.getMethod())) {
            responseContext.getHeaders().add("Allow", "GET,POST,PUT,DELETE,PATCH,HEAD");
            responseContext.setStatus(200);
        }

        
        // Add CORS headers
        String allowOrigin = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_CORS_ALLOWORIGIN);

        if (allowOrigin==null){
            LOG.severe("Property jaqpot.cors.alloworigin is not set!");
        }
        responseContext.getHeaders().add("Access-Control-Allow-Origin", allowOrigin);
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization, subjectid, Accept, total");
        responseContext.getHeaders().add("access-control-expose-headers", "total");
        responseContext.getHeaders().add("Date", new Date().toString());

    }

}
