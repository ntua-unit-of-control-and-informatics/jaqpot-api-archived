/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.filter;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Swagger;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.In;

/**
 *
 * @author hampos
 */
@SwaggerDefinition
public class SwaggerFilter implements ReaderListener {

    @Override
    public void beforeScan(Reader reader, Swagger swgr) {

    }

    @Override
    public void afterScan(Reader reader, Swagger swgr) {
        ApiKeyAuthDefinition apiKeyDefinition = new ApiKeyAuthDefinition();
        apiKeyDefinition.setName("subjectid");
        apiKeyDefinition.setIn(In.HEADER);
        swgr.addSecurityDefinition("subjectid", apiKeyDefinition);
    }

}
