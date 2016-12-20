package org.jaqpot.core.service.client.ambit;

import org.jaqpot.ambitclient.AmbitClient;
import org.jaqpot.ambitclient.model.BundleData;
import org.jaqpot.core.model.dto.bundle.BundleProperties;
import org.jaqpot.core.model.dto.bundle.BundleSubstances;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.study.Studies;

import java.io.Closeable;
import java.util.concurrent.ExecutionException;

/**
 * Created by Angelos Valsamis on 20/12/2016.
 */
public interface Ambit extends Closeable{

    public abstract Dataset generateMopacDescriptors(String var1, String var2) throws ExecutionException, InterruptedException;

    public abstract Dataset getDataset(String var1, String var2) throws ExecutionException, InterruptedException;

    public abstract Dataset getDatasetStructures(String var1, String var2) throws ExecutionException, InterruptedException;

    public abstract BundleSubstances getBundleSubstances(String var1, String var2) throws ExecutionException, InterruptedException;

    public abstract BundleProperties getBundleProperties(String var1, String var2) throws ExecutionException, InterruptedException;

    public abstract Studies getSubstanceStudies(String var1, String var2) throws ExecutionException, InterruptedException;

    public abstract String createBundle(BundleData var1, String var2, String var3) throws ExecutionException, InterruptedException;
}
