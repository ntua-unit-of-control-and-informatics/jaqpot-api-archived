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
package org.jaqpot.core.service.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.dto.aa.AuthToken;
import org.jaqpot.core.model.factory.UserFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

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
    UserHandler userHandler;

    @Inject
    @UnSecure
    Client client;

    Map<String, User> tokenMap;

    @PostConstruct
    private void init() {
        tokenMap = new HashMap<>();
    }

    @Lock(LockType.READ)
    public User getUserFromToken(String token) {
        return tokenMap.get(token);
    }

    public void removeToken(String token) {
        tokenMap.remove(token);
    }

    public void registerUserToken(String token, User user) {
        tokenMap.putIfAbsent(token, user);
    }
    public static final String SSO_HOST = "openam.in-silico.ch",
            SSO_IDENTITY = "https://" + SSO_HOST + "/auth/%s",
            SSO_POLICY = "https://" + SSO_HOST + "/pol",
            /**
             * SSO identity service
             */
            SSOidentity = String.format(SSO_IDENTITY, ""),
            /**
             * SSO authentication service
             */
            SSOauthenticate = String.format(SSO_IDENTITY, "authenticate"), //?uri=service=openldap
            /**
             * SSO policy service
             */
            SSOPolicy = String.format(SSO_POLICY, ""),
            /**
             * SSO identity validation service
             */
            SSOvalidate = String.format(SSO_IDENTITY, "isTokenValid"),
            /**
             * SSO logout/token-invalidation service
             */
            SSOlogout = String.format(SSO_IDENTITY, "logout"),
            /**
             * SSO authorization service
             */
            SSOauthorization = String.format(SSO_IDENTITY, "authorize"),
            SSOattributes = String.format(SSO_IDENTITY, "attributes");

    @Lock(LockType.READ)
    public AuthToken login(String username, String password) throws JaqpotNotAuthorizedException {

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("username", username);
        formData.putSingle("password", password);
        Response response = client.target(SSOauthenticate)
                .request()
                .post(Entity.form(formData));
        String responseValue = response.readEntity(String.class);
        response.close();
        if (response.getStatus() == 401) {
            throw new JaqpotNotAuthorizedException("You cannot login - please, check your credentials.");
        } else {
            AuthToken aToken = new AuthToken();
            aToken.setAuthToken(responseValue.substring(9).replaceAll("\n", ""));
            aToken.setUserName(username);
            User user = userHandler.find(username);
            if (user == null) {
                LOG.log(Level.INFO, "User {0} is valid but doesn''t exist in the database. Creating...", username);
                user = getUserFromSSO(aToken.getAuthToken());
                userHandler.create(user);
                LOG.log(Level.INFO, "User {0} created.", username);
            }
            registerUserToken(aToken.getAuthToken(), user);
            return aToken;
        }
    }

    /*
     CURL example:
     curl  -XPOST -d "tokenid=..." https://opensso.in-silico.ch/auth/isTokenValid -k
     */
    @Lock(LockType.READ)
    public boolean validate(String token) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("tokenid", token);
        Response response = client.target(SSOvalidate)
                .request()
                .post(Entity.form(formData));
        String message = response.readEntity(String.class).trim();
        int status = response.getStatus();
        response.close();
        return "boolean=true".equals(message) && status == 200;
    }

    /**
     * Logs out a user given their authentication token.
     *
     * @param token an authentication token
     * @return
     * @throws org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException
     */
    @Lock(LockType.READ)
    public boolean logout(String token) throws JaqpotNotAuthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("subjectid", token);
        Response response = client.target(SSOlogout)
                .request()
                .post(Entity.form(formData));
        if (200 != response.getStatus()) {
            response.close();
            throw new JaqpotNotAuthorizedException("It seems your token is not valid");
        }
        response.close();
        tokenMap.remove(token);
        int status = response.getStatus();
        return status == 200;
    }

    /**
     * Queries the SSO server to get user attributes and returns a user.
     *
     * @param token authentication token.
     * @return user entity with the capabilities of a new user and the retrieved
     * attributes.
     */
    @Lock(LockType.READ)
    public User getUserFromSSO(String token) {
        User user = UserFactory.newNormalUser();
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("subjectid", token);
        Response response = client.target(SSOattributes)
                .request()
                .post(Entity.form(formData));

        if (200 == response.getStatus()) {
            String attributesList = response.readEntity(String.class);
            Scanner scanner = new Scanner(attributesList);

            String template = "userdetails.attribute.name=%s";
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (String.format(template, "uid").equals(line)) {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        String[] values = line.split("=");
                        if (values.length >= 2) {
                            user.setId(values[1]);
                        }
                    }
                } else if (String.format(template, "mail").equals(line)) {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        String[] values = line.split("=");
                        if (values.length >= 2) {
                            user.setMail(values[1]);
                        }
                    }
                } else if (String.format(template, "userpassword").equals(line)) {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        String[] values = line.split("=");
                        if (values.length >= 2) {
                            user.setHashedPass(values[1]);
                        }
                    }
                } else if (String.format(template, "givenname").equals(line)) {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        String[] values = line.split("=");
                        if (values.length >= 2) {
                            user.setName(values[1]);
                        }
                    }
                }
            }
        } else {
            LOG.log(Level.SEVERE, "SSO attributes responded with status {0} for token {1}", 
                    new Object[]{response.getStatus(), token});
            LOG.log(Level.SEVERE, response.readEntity(String.class));
            LOG.info("Returning null user!");
            return null;
        }
        response.close();
        LOG.log(Level.INFO, "User ID   {0}", user.getId());
        LOG.log(Level.INFO, "User Name {0}", user.getName());
        LOG.log(Level.INFO, "User Mail {0}", user.getMail());
        return user;
    }

    @Lock(LockType.READ)
    public boolean authorize(String token, String httpMethod, String uri) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("subjectid", token);
        formData.putSingle("uri", uri);
        formData.putSingle("action", httpMethod);
        Response response = client.target(SSOauthorization)
                .request()
                .post(Entity.form(formData));
        String message = response.readEntity(String.class).trim();
        int status = response.getStatus();
        response.close();
        return "boolean=true".equals(message) && status == 200;
    }
}
