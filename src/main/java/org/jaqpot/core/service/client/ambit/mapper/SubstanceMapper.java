package org.jaqpot.core.service.client.ambit.mapper;

import org.jaqpot.core.model.dto.dataset.Substance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */
@Mapper
public interface SubstanceMapper {
    SubstanceMapper INSTANCE = Mappers.getMapper( SubstanceMapper.class );

    Substance substanceToSubastance (org.jaqpot.ambitclient.model.dataset.Substance Substance);

}
