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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Provider
@PreMatching
public class InboundRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        /*
         * If api_key is provided by swagger as a URL parameter, this filter
         * will create a subjectid header for A&A.
         */
        MultivaluedMap<String, String> urlParams = requestContext.getUriInfo().getQueryParameters(true);
        String api_key = urlParams.getFirst("api_key");        
        if (api_key != null) {
            List<String> list = new ArrayList<>();
            list.add(api_key);
            requestContext.getHeaders().putIfAbsent("subjectid", list);
        } //TODO Needs testing!!!!!!!        

    }

}
