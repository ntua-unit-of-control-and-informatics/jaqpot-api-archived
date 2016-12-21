package org.jaqpot.core.service.client.ambit.mapper;

import org.jaqpot.core.model.dto.bundle.BundleSubstances;
import org.jaqpot.core.model.dto.dataset.Substance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */

@Mapper
public interface BundleSubstancesMapper {
    BundleSubstancesMapper INSTANCE = Mappers.getMapper( BundleSubstancesMapper.class );

    BundleSubstances bundleSubstancesToBundleSubstances (org.jaqpot.ambitclient.model.dto.bundle.BundleSubstances bundleSubstances);

    Substance substanceListToSubstanceList(org.jaqpot.ambitclient.model.dataset.Substance substanceList);
}
