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
package org.jaqpot.core.service.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.jaqpot.core.properties.PropertyManager;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pantelispanka
 */
@Startup
@Singleton
@DependsOn("PropertyManager")
public class OpenIdServiceConfiguration {

    private static final Logger LOG = Logger.getLogger(OpenIdServiceConfiguration.class.getName());

    @Inject
    PropertyManager propertyManager;

    private URL issuerUri;
    private URL providerConfUrl;
    private OIDCProviderMetadata providerMetadata;
//    private OIDCClientInformation clientInformation;
    private ClientAuthentication oidcClient;
    private static RSAPublicKey providerKey;
    private JWKSource jwkSource;
    
    
    @PostConstruct
    public void initialize() {
        this.createOIDCURLS();
        this.createProviderConf();
        this.getPublicRsaKey();
        this.setUpClient();
        this.setKeySource();
    }

    private void createOIDCURLS() {
        String issuerURI = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OIDC_ISSUER);
        String issuerConf = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OIDC_PROVIDER_CONF);
        try {
            LOG.log(Level.INFO, "Starting OIDC configuration on well Known endpoints: {0}", issuerURI + issuerConf);
            this.issuerUri = new URL(issuerURI);
            this.providerConfUrl = new URL(issuerUri, issuerConf);
            
//            LOG.log(Level.INFO, "OIDC configured on well Known endpoints: {0}", this.providerConfUrl.getHost());
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("OpenIdService Endpoints could not be configured");
        }
    }

    
    private void createProviderConf() {
        try {
            
            InputStream stream = this.providerConfUrl.openStream();
            
            String providerInfo = null;
            try (java.util.Scanner s = new java.util.Scanner(stream)) {
                LOG.log(Level.INFO, "providerConf: {0}", this.providerConfUrl.toURI().toString());
                providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                LOG.log(Level.INFO, "reads: {0}", s.toString());
                LOG.log(Level.INFO, "provider info: {0}", providerInfo);
                this.providerMetadata = OIDCProviderMetadata.parse(providerInfo);
            }
        } catch (ParseException | IOException | URISyntaxException e) {
            throw new InternalServerErrorException("OpenIdService provider metadatacould not be configured " + e.getMessage());
        }

    }

    private void getPublicRsaKey() {
        try {
            JSONObject key = getProviderRSAJWK(providerMetadata.getJWKSetURI().toURL().openStream());
            OpenIdServiceConfiguration.providerKey = RSAKey.parse(key).toRSAPublicKey();
        } catch (JOSEException | IOException | ParseException | java.text.ParseException e) {
            throw new InternalServerErrorException("OpenIdService could not read the public key of the server");
        }
    }

    private JSONObject getProviderRSAJWK(InputStream is) throws ParseException {
        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(is);) {
            while (scanner.hasNext()) {
                sb.append(scanner.next());
            }
        }

        String jsonString = sb.toString();
        JSONObject json = JSONObjectUtils.parse(jsonString);

        JSONArray keyList = (JSONArray) json.get("keys");
        for (Object key : keyList) {
            JSONObject k = (JSONObject) key;
            if (k.get("use").equals("sig") && k.get("kty").equals("RSA")) {
                return k;
            }
        }
        return null;
    }
    
    
    private void setKeySource(){
        URI jwkURL = this.providerMetadata.getJWKSetURI();
        try{
            String jwkUrl = jwkURL.toString();
            this.jwkSource = new RemoteJWKSet(new URL(jwkUrl));
        }catch(MalformedURLException e){
            throw new InternalServerErrorException("Could not read jwkURL", e);
        }
    }

    private void setUpClient() {
        String clientIDProp = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OIDC_CLIENT_ID);
        String clientIDPass = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OIDC_CLIENT_PASS);
        ClientID clientID = new ClientID(clientIDProp);
        Secret clientSecret = new Secret(clientIDPass);
        this.oidcClient = new ClientSecretBasic(clientID, clientSecret);
    }

    public URL getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(URL issuerUri) {
        this.issuerUri = issuerUri;
    }

    public URL getProviderConfUrl() {
        return providerConfUrl;
    }

    public void setProviderConfUrl(URL providerConfUrl) {
        this.providerConfUrl = providerConfUrl;
    }

    public OIDCProviderMetadata getProviderMetadata() {
        return providerMetadata;
    }

    public ClientAuthentication getOidcClient() {
        return oidcClient;
    }
    
    public JWKSource getJWKSource(){
        return jwkSource;
    }

}
