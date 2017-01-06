package org.jaqpot.core.service.client.ambit.mapper;

import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Substance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 19/12/2016.
 */
@Mapper
public interface DataEntryMapper {

    DataEntryMapper INSTANCE = Mappers.getMapper( DataEntryMapper.class );

    DataEntry dataEntryToDataEntry(org.jaqpot.ambitclient.model.dataset.DataEntry dataEntry);

    Substance substanceToSubstance(org.jaqpot.ambitclient.model.dataset.Substance substance);
}
