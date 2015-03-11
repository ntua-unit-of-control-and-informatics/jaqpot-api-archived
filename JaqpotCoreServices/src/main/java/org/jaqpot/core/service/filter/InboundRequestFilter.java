/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author chung
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
