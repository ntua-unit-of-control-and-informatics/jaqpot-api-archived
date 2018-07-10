package org.jaqpot.core.service.client.ambit.mapper;

import org.jaqpot.core.model.dto.bundle.BundleData;
import org.jaqpot.core.model.dto.dataset.EntryId;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */

@Mapper//(uses = SubstanceMapper.class)
public interface BundleDataMapper {
    BundleDataMapper INSTANCE = Mappers.getMapper( BundleDataMapper.class );

    BundleData bundleDataToBundleData (org.jaqpot.ambitclient.model.BundleData bundleData);

    EntryId substanceToSubstance (org.jaqpot.ambitclient.model.dataset.Substance Substance);

}

