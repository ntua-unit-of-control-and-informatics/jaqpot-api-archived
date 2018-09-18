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
package org.jaqpot.core.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pantelispanka
 */
@Startup
@Singleton
@DependsOn("MongoDBEntityManager")
public class OnAppInit {

    private static final Logger LOG = Logger.getLogger(OnAppInit.class.getName());
    
    @Inject
    PropertyManager propertyManager;

    @Inject
    UserHandler userHandler;

    @Inject
    AlgorithmHandler algoHandler;

    @Inject
    DatasetHandler datasetHandler;

    @PostConstruct
    void init() {

        String userToSearch = "guest";
        User user = userHandler.find(userToSearch);
        if (user == null) {
            User initialUser = this.firstUser();
            try {
                userHandler.create(initialUser);
            } catch (JaqpotDocumentSizeExceededException e) {
                e.printStackTrace();
            }
        }
        Algorithm algo = algoHandler.find("weka-svm");
        if (algo == null) {
            List<Algorithm> algos = this.readAlgorithms();

            algos.forEach((alg) -> {
                try {
                    algoHandler.create(alg);
                } catch (JaqpotDocumentSizeExceededException e) {
                    e.printStackTrace();
                }
            });
        }

        Long counted = datasetHandler.countAll();

        if (counted.intValue() == 0) {
            this.restoreDatasets();
        }

    }

    public User firstUser() {
        User initialUser = new User();
        initialUser.setId("guest");
        initialUser.setName("guest");
        Map<String, Integer> cap = new HashMap<>();
        cap.put("models", 180);
        cap.put("reports", 180);
        cap.put("algorithms", 200);
        cap.put("tasksParallel", 80);
        cap.put("substances", 3000);
        cap.put("bibtex", 200);
        cap.put("datasets", 220);
        initialUser.setCapabilities(cap);
        Map<String, Integer> pub = new HashMap<>();
        pub.put("models", 100);
        pub.put("algorithms", 10);
        pub.put("substances", 100);
        pub.put("bibtex", 100);
        initialUser.setPublicationRatePerWeek(pub);
        return initialUser;
    }

    public List<Algorithm> readAlgorithms() {

        List<Algorithm> algos = new ArrayList<>();
        algos.addAll(this.wekaAlgorithms());
        algos.addAll(this.pythonAlgorithms());
        algos.addAll(this.experimentalDesigns());
        algos.addAll(this.readacross());
        algos.addAll(this.pksim());
        algos.addAll(this.ocpu_lm());
        return algos;
    }

    private String getFile(String filename) {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(filename));
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
        return result;
    }

    private void restoreDatasets() {
        ObjectMapper mapper = new ObjectMapper();
        List<Dataset> datasets = new ArrayList<>();
        String datasetString = null;
        try {
            datasetString = this.getFile("datasets/Gajewicz_10_29.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Gajewicz_10_29_class.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Gajewicz_18_29.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Gajewicz_18_29_class.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Gajewicz_8_29.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Gajewicz_8_29_class.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Walkey_28_25.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Walkey_28_76.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Walkey_56_25.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Walkey_56_76.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Walkey_84_25.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/Walkey_84_76.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/corona-exp.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
            datasetString = this.getFile("datasets/interlab-dummy.json");
            datasets.add(mapper.readValue(datasetString, Dataset.class));
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
        datasets.forEach((dataset) -> {
            try{
                try {
                    datasetHandler.create(dataset);
                } catch (JaqpotDocumentSizeExceededException e) {
                    e.printStackTrace();
                }
            }catch(IllegalArgumentException e){
                LOG.log(Level.SEVERE, e.getMessage());
            }
        });
    }

    private List<Algorithm> wekaAlgorithms() {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Algorithm>> algoListType = new TypeReference<List<Algorithm>>() {
        };
        List<Algorithm> algos = new ArrayList<>();
        try {
            String result = this.getFile("algorithms/jaqpot-algorithms.json");
            algos = mapper.readValue(result, algoListType);
            for (Algorithm algo : algos) {
                String trainingUri = algo.getTrainingService();
                if (trainingUri != null) {
                    URI trainUriFromFile = new URI(trainingUri);
                    String pathFromFile = trainUriFromFile.getPath();
                    String wekalgohost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_ALGORITHMS);
                    URI hostUrl = new URI(wekalgohost);
                    URI wekaAlgoTrainingService = hostUrl.resolve(pathFromFile);
                    algo.setTrainingService(wekaAlgoTrainingService.toString());
                }
                String predictingUri = algo.getPredictionService();
                if (predictingUri != null) {
                    URI predictUriFromFile = new URI(predictingUri);
                    String pathFromFile = predictUriFromFile.getPath();
                    String wekalgohost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_ALGORITHMS);
                    URI hostUrl = new URI(wekalgohost);
                    URI wekaAlgoPredictingService = hostUrl.resolve(pathFromFile);
                    algo.setPredictionService(wekaAlgoPredictingService.toString());
                }
                String reportingUri = algo.getReportService();
                if (reportingUri != null) {
                    URI reportingUriFromFile = new URI(reportingUri);
                    String pathFromFile = reportingUriFromFile.getPath();
                    String wekalgohost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_ALGORITHMS);
                    URI hostUrl = new URI(wekalgohost);
                    URI wekaAlgoReportingService = hostUrl.resolve(pathFromFile);
                    algo.setReportService(wekaAlgoReportingService.toString());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return algos;

    }

    private List<Algorithm> pythonAlgorithms() {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Algorithm>> algoListType = new TypeReference<List<Algorithm>>() {
        };
        List<Algorithm> algos = new ArrayList<>();
        try {
            String result = this.getFile("algorithms/python-algorithms.json");
            algos = mapper.readValue(result, algoListType);
            for (Algorithm algo : algos) {
                String trainingUri = algo.getTrainingService();
                if (trainingUri != null) {
                    URI trainUriFromFile = new URI(trainingUri);
                    String pathFromFile = trainUriFromFile.getPath();
                    String pythonalgohost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PYTHON_ALGORITHMS_HOST);
                    URI hostUrl = new URI(pythonalgohost);
                    URI pythonAlgoTrainingService = hostUrl.resolve(pathFromFile);
                    algo.setTrainingService(pythonAlgoTrainingService.toString());
                }
                String predictingUri = algo.getPredictionService();
                if (predictingUri != null) {
                    URI predictUriFromFile = new URI(predictingUri);
                    String pathFromFile = predictUriFromFile.getPath();
                    String pythonalgohost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PYTHON_ALGORITHMS_HOST);
                    URI hostUrl = new URI(pythonalgohost);
                    URI pythonAlgoPredictingService = hostUrl.resolve(pathFromFile);
                    algo.setPredictionService(pythonAlgoPredictingService.toString());
                }
                String reportingUri = algo.getReportService();
                if (reportingUri != null) {
                    URI reportingUriFromFile = new URI(reportingUri);
                    String pathFromFile = reportingUriFromFile.getPath();
                    String pythonalgohost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PYTHON_ALGORITHMS_HOST);
                    URI hostUrl = new URI(pythonalgohost);
                    URI pythonAlgoReportingService = hostUrl.resolve(pathFromFile);
                    algo.setReportService(pythonAlgoReportingService.toString());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return algos;
    }

    private List<Algorithm> experimentalDesigns() {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Algorithm>> algoListType = new TypeReference<List<Algorithm>>() {
        };
        List<Algorithm> algos = new ArrayList<>();
        try {
            String result = this.getFile("algorithms/exp-design.json");
            algos = mapper.readValue(result, algoListType);
            for (Algorithm algo : algos) {
                String trainingUri = algo.getTrainingService();
                if (trainingUri != null) {
                    URI trainUriFromFile = new URI(trainingUri);
                    String pathFromFile = trainUriFromFile.getPath();
                    String expDesHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_EXPERIMENTAL_DESIGNS_HOST);
                    URI hostUrl = new URI(expDesHost);
                    URI expDesTrainingService = hostUrl.resolve(pathFromFile);
                    algo.setTrainingService(expDesTrainingService.toString());
                }
                String predictingUri = algo.getPredictionService();
                if (predictingUri != null) {
                    URI predictUriFromFile = new URI(predictingUri);
                    String pathFromFile = predictUriFromFile.getPath();
                    String expDesHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_EXPERIMENTAL_DESIGNS_HOST);
                    URI hostUrl = new URI(expDesHost);
                    URI expDesPredictingService = hostUrl.resolve(pathFromFile);
                    algo.setPredictionService(expDesPredictingService.toString());
                }
                String reportingUri = algo.getReportService();
                if (reportingUri != null) {
                    URI reportingUriFromFile = new URI(reportingUri);
                    String pathFromFile = reportingUriFromFile.getPath();
                    String expDesHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_EXPERIMENTAL_DESIGNS_HOST);
                    URI hostUrl = new URI(expDesHost);
                    URI expDesReportingService = hostUrl.resolve(pathFromFile);
                    algo.setReportService(expDesReportingService.toString());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return algos;
    }

    private List<Algorithm> readacross() {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Algorithm>> algoListType = new TypeReference<List<Algorithm>>() {
        };
        List<Algorithm> algos = new ArrayList<>();
        try {
            String result = this.getFile("algorithms/jaqpot-readacross.json");
            algos = mapper.readValue(result, algoListType);
            for (Algorithm algo : algos) {
                String trainingUri = algo.getTrainingService();
                if (trainingUri != null) {
                    URI trainUriFromFile = new URI(trainingUri);
                    String pathFromFile = trainUriFromFile.getPath();
                    String jaqReadHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_READACROSS);
                    URI hostUrl = new URI(jaqReadHost);
                    URI readaccrTrainingService = hostUrl.resolve(pathFromFile);
                    algo.setTrainingService(readaccrTrainingService.toString());
                }
                String predictingUri = algo.getPredictionService();
                if (predictingUri != null) {
                    URI predictUriFromFile = new URI(predictingUri);
                    String pathFromFile = predictUriFromFile.getPath();
                    String jaqReadHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_READACROSS);
                    URI hostUrl = new URI(jaqReadHost);
                    URI readaccrPredictingService = hostUrl.resolve(pathFromFile);
                    algo.setPredictionService(readaccrPredictingService.toString());
                }
                String reportingUri = algo.getReportService();
                if (reportingUri != null) {
                    URI reportingUriFromFile = new URI(reportingUri);
                    String pathFromFile = reportingUriFromFile.getPath();
                    String jaqReadHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_READACROSS);
                    URI hostUrl = new URI(jaqReadHost);
                    URI readaccrReportingService = hostUrl.resolve(pathFromFile);
                    algo.setReportService(readaccrReportingService.toString());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return algos;

    }

    private List<Algorithm> pksim() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Algorithm>> algoListType = new TypeReference<List<Algorithm>>() {
        };
        List<Algorithm> algos = new ArrayList<>();
        try {
            String result = this.getFile("algorithms/pk-sim.json");
            algos = mapper.readValue(result, algoListType);
            for (Algorithm algo : algos) {
                String trainingUri = algo.getTrainingService();
                if (trainingUri != null) {
                    URI trainUriFromFile = new URI(trainingUri);
                    String pathFromFile = trainUriFromFile.getPath();
                    String pkHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.PKSIM_BASE);
                    URI hostUrl = new URI(pkHost);
                    URI pkTrainingService = hostUrl.resolve(pathFromFile);
                    algo.setTrainingService(pkTrainingService.toString());
                }
                String predictingUri = algo.getPredictionService();
                if (predictingUri != null) {
                    URI predictUriFromFile = new URI(predictingUri);
                    String pathFromFile = predictUriFromFile.getPath();
                    String pkHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.PKSIM_BASE);
                    URI hostUrl = new URI(pkHost);
                    URI pkPredictingService = hostUrl.resolve(pathFromFile);
                    algo.setPredictionService(pkPredictingService.toString());
                }
                String reportingUri = algo.getReportService();
                if (reportingUri != null) {
                    URI reportingUriFromFile = new URI(reportingUri);
                    String pathFromFile = reportingUriFromFile.getPath();
                    String pkHost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.PKSIM_BASE);
                    URI hostUrl = new URI(pkHost);
                    URI pkReportingService = hostUrl.resolve(pathFromFile);
                    algo.setReportService(pkReportingService.toString());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return algos;
    }

    private List<Algorithm> ocpu_lm() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Algorithm>> algoListType = new TypeReference<List<Algorithm>>() {
        };
        List<Algorithm> algos = new ArrayList<>();
        String result = this.getFile("algorithms/ocpu_lm.json");
        try {
            algos = mapper.readValue(result, algoListType);
            for (Algorithm algo : algos) {
                String trainingUri = algo.getTrainingService();
                if (trainingUri != null) {
                    URI trainUriFromFile = new URI(trainingUri);
                    String pathFromFile = trainUriFromFile.getPath();
                    String ocpu_lmhost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OCPU_LM_BASE);
                    URI hostUrl = new URI(ocpu_lmhost);
                    URI ocpuTrainingService = hostUrl.resolve(pathFromFile);
                    algo.setTrainingService(ocpuTrainingService.toString());
                }
                String predictingUri = algo.getPredictionService();
                if (predictingUri != null) {
                    URI predictUriFromFile = new URI(predictingUri);
                    String pathFromFile = predictUriFromFile.getPath();
                    String ocpu_lmhost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OCPU_LM_BASE);
                    URI hostUrl = new URI(ocpu_lmhost);
                    URI ocpuPredictingService = hostUrl.resolve(pathFromFile);
                    algo.setPredictionService(ocpuPredictingService.toString());
                }
                String reportingUri = algo.getReportService();
                if (reportingUri != null) {
                    URI reportingUriFromFile = new URI(reportingUri);
                    String pathFromFile = reportingUriFromFile.getPath();
                    String ocpu_lmhost = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.OCPU_LM_BASE);
                    URI hostUrl = new URI(ocpu_lmhost);
                    URI ocpuReportingService = hostUrl.resolve(pathFromFile);
                    algo.setReportService(ocpuReportingService.toString());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return algos;
    }

}
