/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.ReaderListener;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
//import io.swagger.annotations.Contact;
//import io.swagger.annotations.Info;
//import io.swagger.annotations.SwaggerDefinition;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
//import io.swagger.jaxrs.Reader;
//import io.swagger.jaxrs.config.ReaderListener;
//import io.swagger.models.Swagger;
//import io.swagger.models.auth.ApiKeyAuthDefinition;
//import io.swagger.models.auth.In;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author hampos
 */
//@SwaggerDefinition(
@OpenAPIDefinition(
        info = @Info(
                title = "ModelsBase API",
                description = "ModelsBase API v5 is the 5t"
                        + "h version of a YAQP, a RESTful web platform which can"
                        + " be used to train machine learning models and use "
                        + "them to obtain toxicological predictions for given "
                        + "chemical compounds or engineered nano materials. "
                        + "Jaqpot v4 has integrated read-across, optimal experimental design,"
                        + " interlaboratory comparison, biokinetics and dose response modelling"
                        + " functionalities. The project is developed in Java8 and JEE7 by the"
                        + " <a href=\"http://www.chemeng.ntua.gr/labs/control_lab/\"> Unit of"
                        + " Process Control"
                        + " and Informatics in the School of Chemical Engineering </a> at"
                        + " the  <a href=\"https://www.ntua.gr/en/\"> National"
                        + " Technical University of Athens.</a> ",
                version = "6.0.24",
                contact = @Contact(name = "Pantelis Karatzas,"
                        + " Pantelis Sopasakis, Angelos Valsamis,"
                        + " Philip Doganis, Periklis Tsiros, Iasonas Sotiropoulos"
                        + " Haralambos Sarimveis", email = "contact@euclia.org",
                        url = "https://github.com/KinkyDesign/jaqpot-web/issues")
        )
)
public class SwaggerFilter implements ReaderListener {

    @Override
    //public void beforeScan(Reader reader, Swagger swgr) {
    public void beforeScan(Reader reader, OpenAPI swgr) {

    }

    @Override
    //public void afterScan(Reader reader, Swagger swgr) {
    public void afterScan(Reader reader, OpenAPI swgr) {
        SecurityScheme securityScheme = new SecurityScheme();
        //ApiKeyAuthDefinition apiKeyDefinition = new ApiKeyAuthDefinition();
        securityScheme.setIn(SecurityScheme.In.HEADER);
        securityScheme.setName("Authorization");
        securityScheme.setType(SecurityScheme.Type.APIKEY);
        SecurityRequirement sr = new SecurityRequirement();
        sr.addList("Authorization");
        swgr.addSecurityItem(sr);
        //apiKeyDefinition.setName("Authorization");
        //apiKeyDefinition.setIn(In.HEADER);
        //apiKeyDefinition.setType("apiKey");
        //swgr.addSecurityDefinition("apiKey", apiKeyDefinition);
    }

}
