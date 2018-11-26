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
package org.jaqpot.core.service.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.factory.UserFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Singleton
public class AAService {

    private static final Logger LOG = Logger.getLogger(AAService.class.getName());

    @EJB
    OpenIdServiceConfiguration oidcConf;

    @EJB
    UserHandler userHandler;

    @Inject
    @UnSecure
    Client client;

    public AccessToken getAccessToken(String username, String password) throws JaqpotNotAuthorizedException {

        Secret pass = new Secret(password);
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(username, pass);
        Scope scope = new Scope("read", "write");
        TokenRequest request = new TokenRequest(
                oidcConf.getProviderMetadata().getTokenEndpointURI(),
                oidcConf.getOidcClient(), passwordGrant, scope);
        TokenResponse response = null;
        
        try {
            response = TokenResponse.parse(request.toHTTPRequest().send());
        } catch (ParseException | SerializeException | IOException e) {
            throw new InternalServerErrorException("");
        }
        if (!response.indicatesSuccess()) {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            
            throw new JaqpotNotAuthorizedException("Problems while getting access token" + errorResponse.getErrorObject().getDescription());
        }
        AccessTokenResponse successResponse = (AccessTokenResponse) response;
        AccessToken accessToken = successResponse.getTokens().getAccessToken();
        return accessToken;
    }

    public boolean validateAccessToken(String accessToken) throws JaqpotNotAuthorizedException {
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        OIDCProviderMetadata omd = oidcConf.getProviderMetadata();
        JWKSource keySource = this.oidcConf.getJWKSource();
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        SecurityContext ctx = null;
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, ctx);
            this.getUserFromSSO(accessToken);
        } catch (java.text.ParseException | BadJOSEException | JOSEException e) {
            throw new JaqpotNotAuthorizedException("It seems your token is not valid", "401 : " + e.getMessage());
        }
        return true;
    }

    public JWTClaimsSet getClaimsFromAccessToken(String accessToken) throws JaqpotNotAuthorizedException {
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();

        JWKSource keySource = this.oidcConf.getJWKSource();
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        SecurityContext ctx = null;
        JWTClaimsSet claimsSet = null;
        try {
            claimsSet = jwtProcessor.process(accessToken, ctx);
        } catch (java.text.ParseException | BadJOSEException | JOSEException e) {
            throw new JaqpotNotAuthorizedException("Problems " + e.getMessage());
        }
        return claimsSet;
    }
    
    
    public boolean isAdmin(String accessToken){
        JWTClaimsSet claims = null;
        List<String> claim = null;
        try{
            claims = this.getClaimsFromAccessToken(accessToken);
            claim = (List<String>) claims.getClaim("groups");
        }catch(JaqpotNotAuthorizedException | NullPointerException e){
            return false;
        }
        return claim.contains("/Administrator");        
    }


//    /**
//     * Logs out a user given their authentication token.
//     *
//     * @param token an authentication token
//     * @return
//     * @throws org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException
//     */
//    public boolean logout(String token) throws JaqpotNotAuthorizedException {
//        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
//        formData.putSingle("subjectid", token);
//        Response response = client.target(SSOlogout)
//                .request()
//                .post(Entity.form(formData));
//        if (200 != response.getStatus()) {
//            response.close();
//            throw new JaqpotNotAuthorizedException("It seems your token is not valid", "401");
//        }
//        int status = response.getStatus();
//        tokenMap.remove(token);
//        response.close();
//        return status == 200;
//    }
    /**
     * Queries the SSO server to get user attributes and returns a user.
     *
     * @param accessToken
     * 
     * @return user entity with the capabilities of a new user and the retrieved
     * attributes.
     * @throws org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException
     */
    public User getUserFromSSO(String accessToken) throws JaqpotNotAuthorizedException {
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();

//        OIDCProviderMetadata omd = oidcConf.getProviderMetadata();
        JWKSource keySource = this.oidcConf.getJWKSource();
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        SecurityContext ctx = null;
        JWTClaimsSet claimsSet = null;
        try {
            claimsSet = jwtProcessor.process(accessToken, ctx);
        } catch (java.text.ParseException | BadJOSEException | JOSEException e) {
            throw new JaqpotNotAuthorizedException("Problems " + e.getMessage());
        }

        String userId = claimsSet.getSubject();
        User user = userHandler.find(userId);
        if (user == null) {
            User userToSave = UserFactory.newNormalUser();
            try {
                userToSave.setId(userId);
                userToSave.setMail(claimsSet.getStringClaim("email"));
                userToSave.setName(claimsSet.getStringClaim("preferred_username"));
                userHandler.create(userToSave);
            } catch (java.text.ParseException e) {
                throw new InternalServerErrorException("Could not create user on database", e);
            } catch (JaqpotDocumentSizeExceededException e) {
                e.printStackTrace();
            }
            return userToSave;
        }
        return user;
    }

//    public boolean authorize(String token, String httpMethod, String uri) throws JaqpotNotAuthorizedException {
//        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
//        formData.putSingle("subjectid", token);
//        formData.putSingle("uri", uri);
//        formData.putSingle("action", httpMethod);
//        Response response = client.target(SSOauthorization)
//                .request()
//                .post(Entity.form(formData));
//        String message = response.readEntity(String.class).trim();
//        int status = response.getStatus();
//        if (response.getStatus() == 401) {
//            response.close();
//            throw new JaqpotNotAuthorizedException("Invalid or out-of-date token - please, try to login again.");
//        } else {
//            response.close();
//            return "boolean=true".equals(message) && status == 200;
//        }
//    }
}
