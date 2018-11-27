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

import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.exceptions.JaqpotWebException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class ValidationService {

    @EJB
    TaskHandler taskHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    @UnSecure
    Client client;

    public Object[] trainAndTest(String algorithmURI, String trainingDataset, String testingDataset, String predictionFeature, String algorithmParameters, String transformations, String scaling, String subjectId) throws JaqpotWebException {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.add("dataset_uri", trainingDataset);
        params.add("prediction_feature", predictionFeature);
        params.add("parameters", algorithmParameters);
        params.add("transformations", transformations);
        params.add("scaling", scaling);
                
        Task trainTask = client.target(algorithmURI)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", subjectId)
                .post(Entity.form(params), Task.class);
        String trainTaskURI = algorithmURI.split("algorithm")[0] + "task/" + trainTask.getId();
        while (trainTask.getStatus().equals(Task.Status.RUNNING)
                || trainTask.getStatus().equals(Task.Status.QUEUED)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {

            }
            trainTask = client.target(trainTaskURI)
                    .request()
                    .header("Authorization", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Task.class);
        }
        if (!trainTask.getStatus().equals(Task.Status.COMPLETED)) {
            throw new JaqpotWebException(trainTask.getErrorReport());
        }
        String modelURI = trainTask.getResultUri();
        
        params.clear();
        params.add("dataset_uri", testingDataset);
        Task predictionTask = client.target(trainTask.getResultUri())
                .request()
                .header("Authorization", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.form(params), Task.class);
        String predictionTaskURI = modelURI.split("model")[0] + "task/" + predictionTask.getId();
        while (predictionTask.getStatus().equals(Task.Status.RUNNING)
                || predictionTask.getStatus().equals(Task.Status.QUEUED)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {

            }
            predictionTask = client.target(predictionTaskURI)
                    .request()
                    .header("Authorization", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Task.class);
        }
        if (!predictionTask.getStatus().equals(Task.Status.COMPLETED)) {
            throw new JaqpotWebException(predictionTask.getErrorReport());

        }
        Model model = client.target(modelURI)
                .request()
                .header("Authorization", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .get(Model.class);
        Object[] result = new Object[5];
        result[0] = predictionTask.getResultUri();
        result[1] = predictionFeature;
        result[2] = model.getPredictedFeatures().get(0);
        result[3] = model.getIndependentFeatures().size();
        result[4] = modelURI;
        return result;
    }

}
