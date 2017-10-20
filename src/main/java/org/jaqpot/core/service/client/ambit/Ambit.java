package org.jaqpot.core.service.client.ambit;

import org.jaqpot.core.model.dto.bundle.BundleData;
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

    Dataset generateMopacDescriptors(String var1, String var2) throws ExecutionException, InterruptedException;

    Dataset getDataset(String var1, String var2) throws ExecutionException, InterruptedException;

    Dataset getDatasetStructures(String var1, String var2) throws ExecutionException, InterruptedException;

    BundleSubstances getBundleSubstances(String var1, String var2) throws ExecutionException, InterruptedException;

    BundleProperties getBundleProperties(String var1, String var2) throws ExecutionException, InterruptedException;

    Studies getSubstanceStudies(String var1, String var2) throws ExecutionException, InterruptedException;

    BundleData getSubstancesBySubstanceOwner(String var1, String var2) throws ExecutionException, InterruptedException;
}
