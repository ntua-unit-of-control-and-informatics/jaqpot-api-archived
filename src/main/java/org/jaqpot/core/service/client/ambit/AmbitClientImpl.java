package org.jaqpot.core.service.client.ambit;

import org.jaqpot.ambitclient.AmbitClient;
import org.jaqpot.ambitclient.model.BundleData;
import org.jaqpot.ambitclient.model.dataset.Dataset;
import org.jaqpot.ambitclient.model.dto.bundle.BundleProperties;
import org.jaqpot.ambitclient.model.dto.bundle.BundleSubstances;
import org.jaqpot.ambitclient.model.dto.study.Studies;
import org.jaqpot.core.service.client.ambit.mapper.BundlePropertiesMapper;
import org.jaqpot.core.service.client.ambit.mapper.BundleSubstancesMapper;
import org.jaqpot.core.service.client.ambit.mapper.DatasetMapper;
import org.jaqpot.core.service.client.ambit.mapper.StudiesMapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.jaqpot.ambitclient.AmbitClientFactory.createNewClient;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */
public class AmbitClientImpl implements Ambit {

    private final AmbitClient ambitClient;

    private final JacksonSerializer jacksonSerializer;

    private static final Logger LOG = Logger.getLogger(AmbitClientImpl.class.getName());


    public AmbitClientImpl(){
        ambitClient = null;
        jacksonSerializer = null;
    }

    public AmbitClientImpl(String ambitBase, JacksonSerializer jacksonSerializer) {
        this.ambitClient = createNewClient(ambitBase, jacksonSerializer);
        this.jacksonSerializer = jacksonSerializer;
    }


    @Override
    public org.jaqpot.core.model.dto.dataset.Dataset generateMopacDescriptors(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = ambitClient.generateMopacDescriptors(var1,var2);
        Dataset dataset = datasetFuture.get();
        return DatasetMapper.INSTANCE.datasetToDataset(dataset);
    }

    @Override
    public org.jaqpot.core.model.dto.dataset.Dataset getDataset(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = ambitClient.getDataset(var1,var2);
        Dataset dataset = datasetFuture.get();
        return DatasetMapper.INSTANCE.datasetToDataset(dataset);
    }

    @Override
    public org.jaqpot.core.model.dto.dataset.Dataset getDatasetStructures(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = ambitClient.getDatasetStructures(var1,var2);
        Dataset dataset = datasetFuture.get();
        return DatasetMapper.INSTANCE.datasetToDataset(dataset);
    }

    @Override
    public org.jaqpot.core.model.dto.bundle.BundleSubstances getBundleSubstances(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<BundleSubstances> bundleSubstancesFuture = ambitClient.getBundleSubstances(var1,var2);
        BundleSubstances bundleSubstances = bundleSubstancesFuture.get();
        return BundleSubstancesMapper.INSTANCE.bundleSubstancesToBundleSubstances(bundleSubstances);
    }

    @Override
    public org.jaqpot.core.model.dto.bundle.BundleProperties getBundleProperties(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<BundleProperties> bundlePropertiesFuture = ambitClient.getBundleProperties(var1,var2);
        BundleProperties bundleProperties = bundlePropertiesFuture.get();
        return BundlePropertiesMapper.INSTANCE.bundlePropertiesToBundlePropertiesMapper(bundleProperties);
    }

    @Override
    public org.jaqpot.core.model.dto.study.Studies getSubstanceStudies(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Studies> studiesFuture = ambitClient.getSubstanceStudies(var1,var2);
        Studies studies = studiesFuture.get();
        return StudiesMapper.INSTANCE.studiesToStudiesMapper(studies);
    }

    @Override
    public String createBundle(BundleData var1, String var2, String var3) throws ExecutionException, InterruptedException {
        CompletableFuture<String> bundleFuture = ambitClient.createBundle(var1,var2,var3);
        return bundleFuture.get();
    }

    @Override
    public void close() throws IOException {
        ambitClient.close();
    }
}
