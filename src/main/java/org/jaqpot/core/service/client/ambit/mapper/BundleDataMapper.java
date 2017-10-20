package org.jaqpot.core.service.client.ambit.mapper;

import org.jaqpot.core.model.dto.bundle.BundleData;
import org.jaqpot.core.model.dto.dataset.Substance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */

@Mapper
public interface BundleDataMapper {
    BundleDataMapper INSTANCE = Mappers.getMapper( BundleDataMapper.class );

    BundleData bundleDataToBundleDataMapper (org.jaqpot.ambitclient.model.BundleData bundleData);

    Substance substanceToSubstanceMapper(org.jaqpot.ambitclient.model.dataset.Substance substance);

}

