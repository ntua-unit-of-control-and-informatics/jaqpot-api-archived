package org.jaqpot.core.service.client.ambit;

import org.jaqpot.ambitclient.AmbitClient;
import org.jaqpot.ambitclient.model.BundleData;
import org.jaqpot.ambitclient.model.dataset.Dataset;
import org.jaqpot.ambitclient.model.dto.bundle.BundleProperties;
import org.jaqpot.ambitclient.model.dto.bundle.BundleSubstances;
import org.jaqpot.ambitclient.model.dto.study.Studies;
import org.jaqpot.core.service.client.ambit.mapper.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.jaqpot.ambitclient.AmbitClientFactory.createNewClient;


/**
 * Created by Angelos Valsamis on 12/12/2016.
 */
public class AmbitClientImpl implements Ambit {

    private final AmbitClient ambit;

    private final JacksonSerializer jacksonSerializer;

    private static final Logger LOG = Logger.getLogger(AmbitClientImpl.class.getName());


    public AmbitClientImpl(){
        ambit = null;
        jacksonSerializer = null;
    }

    public AmbitClientImpl(String ambitBase, JacksonSerializer jacksonSerializer) {
        this.ambit = createNewClient(ambitBase, jacksonSerializer);
        this.jacksonSerializer = jacksonSerializer;
    }


    @Override
    public org.jaqpot.core.model.dto.dataset.Dataset generateMopacDescriptors(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = ambit.generateMopacDescriptors(var1,var2);
        Dataset dataset = datasetFuture.get();
        return DatasetMapper.INSTANCE.datasetToDataset(dataset);
    }

    @Override
    public org.jaqpot.core.model.dto.dataset.Dataset getDataset(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = ambit.getDataset(var1,var2);
        Dataset dataset = datasetFuture.get();
        return DatasetMapper.INSTANCE.datasetToDataset(dataset);
    }

    @Override
    public org.jaqpot.core.model.dto.dataset.Dataset getDatasetStructures(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = ambit.getDatasetStructures(var1,var2);
        Dataset dataset = datasetFuture.get();
        return DatasetMapper.INSTANCE.datasetToDataset(dataset);
    }

    @Override
    public org.jaqpot.core.model.dto.bundle.BundleSubstances getBundleSubstances(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<BundleSubstances> bundleSubstancesFuture = ambit.getBundleSubstances(var1,var2);
        BundleSubstances bundleSubstances = bundleSubstancesFuture.get();
        return BundleSubstancesMapper.INSTANCE.bundleSubstancesToBundleSubstances(bundleSubstances);
    }

    @Override
    public org.jaqpot.core.model.dto.bundle.BundleProperties getBundleProperties(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<BundleProperties> bundlePropertiesFuture = ambit.getBundleProperties(var1,var2);
        BundleProperties bundleProperties = bundlePropertiesFuture.get();
        return BundlePropertiesMapper.INSTANCE.bundlePropertiesToBundlePropertiesMapper(bundleProperties);
    }

    @Override
    public org.jaqpot.core.model.dto.study.Studies getSubstanceStudies(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<Studies> studiesFuture = ambit.getSubstanceStudies(var1,var2);
        Studies studies = studiesFuture.get();
        return StudiesMapper.INSTANCE.studiesToStudiesMapper(studies);
    }

    @Override
    public org.jaqpot.core.model.dto.bundle.BundleData getSubstancesBySubstanceOwner(String var1, String var2) throws ExecutionException, InterruptedException {
        CompletableFuture<BundleData> bundleFuture = ambit.getSubstancesBySubstanceOwner(var1,var2);
        BundleData bundleData = bundleFuture.get();
        return BundleDataMapper.INSTANCE.bundleDataToBundleDataMapper(bundleData);    }

    @Override
    public void close() throws IOException {
        ambit.close();
    }
}
