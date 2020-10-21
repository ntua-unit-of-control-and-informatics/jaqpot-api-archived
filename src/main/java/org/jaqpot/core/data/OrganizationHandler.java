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
package org.jaqpot.core.data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.asynchttpclient.Response;
import org.jaqpot.core.accounts.AccountsHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.service.exceptions.JaqpotWebException;
import org.jaqpot.core.service.quotas.JQuotsSerializer;
import org.jaqpot.core.service.quotas.QuotsClient;
//import xyz.euclia.euclia.accounts.client.models.ErrorReport;
import xyz.euclia.euclia.accounts.client.models.Organization;
import xyz.euclia.jquots.serialize.Serializer;



/**
 *
 * @author pantelispanka
 */
@Stateless
public class OrganizationHandler{
    
    @EJB
    AccountsHandler accountsHandler;

    @EJB
    QuotsClient quotsClient;

    public Organization find(String id, String apiKey) throws JaqpotWebException {
        Future<Response> eucliaUser = this.accountsHandler.getClient().getUser(id, apiKey);
        Organization org = new Organization();
        try{
            Response resp = eucliaUser.get();
            if(resp.getStatusCode() >= 300){
                xyz.euclia.euclia.accounts.client.models.ErrorReport er = this.quotsClient.getSerializer().parse(eucliaUser.get().getResponseBody(), xyz.euclia.euclia.accounts.client.models.ErrorReport.class);
                ErrorReport jer = new ErrorReport();
                jer.setHttpStatus(er.getStatus());
                jer.setMessage(er.getMessage());
                throw new JaqpotWebException(jer);
            }else{
                org = this.quotsClient.getSerializer().parse(eucliaUser.get().getResponseBody(), Organization.class);
//                user = users[0];
            }
        }catch(InterruptedException | ExecutionException e){
            throw new InternalServerErrorException(e.getMessage());
        }
        return org;
    }
    
//    @Inject
//    @MongoDB
//    JaqpotEntityManager em;
//
//    public OrganizationHandler() {
//        super(Organization.class);
//    }
//
//    @Override
//    protected JaqpotEntityManager getEntityManager() {
//        return em;
//    }
//    
//    public List<Organization> findAllWithPattern(Map<String, Object> searchFor) {
//        Map<String, Object> properties = new HashMap<>();
//        searchFor.keySet().forEach((key) -> {
//            Object pattern = ".*" + searchFor.get(key) + ".*";
//            properties.put(key, pattern);
//        });
//
//        List<String> fields = new ArrayList<>();
//        fields.add("_id");
//
//        return em.findAllWithReqexp(Organization.class, properties, fields, 0, Integer.MAX_VALUE);
//    }
    
}
