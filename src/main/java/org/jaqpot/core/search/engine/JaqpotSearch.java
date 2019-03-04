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
package org.jaqpot.core.search.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.BadRequestException;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.jaqpot.core.search.dto.ElasticResponse;

/**
 *
 * @author pantelispanka
 */
@Stateless
public class JaqpotSearch {

    Logger logger = Logger.getLogger(JaqpotSearch.class.getName());

    @EJB
    JaqpotElasticSearch jes;

    @EJB
    ElasticQueries eq;

    public void term(String term, int from, int size) {
        String query = eq.getQuery();
        String queryFromed = String.format(query, from, size, term);
        HttpEntity httpEntity = new NStringEntity(queryFromed, ContentType.APPLICATION_JSON);
        String path = "/jaqpotindexdev/_search";
        Request req = new Request("GET", path);
        req.setEntity(httpEntity);
        try{
            Response resp = this.jes.getClient().performRequest(req);
        }catch(IOException e){
            throw new BadRequestException("Error!  Could not complete request");
        }
        
        this.jes.getClient().performRequestAsync(req, indexListener());
    }

    public ResponseListener indexListener() {
        ResponseListener responseListener = new ResponseListener() {

            @Override
            public void onFailure(Exception e) {
                logger.log(Level.SEVERE, "Failed");
            }

            @Override
            public void onSuccess(Response rspns) {
                ObjectMapper mapper = new ObjectMapper();
                try {                
                    ElasticResponse elResp = mapper.readValue(rspns.getEntity().getContent(), ElasticResponse.class );
                } catch (IOException ex) {
                    Logger.getLogger(JaqpotSearch.class.getName()).log(Level.SEVERE, null, ex);
                }
                logger.log(Level.INFO, rspns.getEntity().toString());              
            }

        };
        return responseListener;
    }
;

}
