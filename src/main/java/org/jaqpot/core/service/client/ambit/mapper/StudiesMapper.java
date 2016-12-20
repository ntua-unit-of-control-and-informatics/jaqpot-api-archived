package org.jaqpot.core.service.client.ambit.mapper;

        import org.jaqpot.core.model.dto.study.*;
        import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

        import javax.ws.rs.Produces;

/**
 * Created by Angelos Valsamis on 19/12/2016.
 */
@Mapper
public interface StudiesMapper {

    org.jaqpot.core.service.client.ambit.mapper.StudiesMapper INSTANCE = Mappers.getMapper( org.jaqpot.core.service.client.ambit.mapper.StudiesMapper.class );

    Studies studiesToStudiesMapper(org.jaqpot.ambitclient.model.dto.study.Studies studies);

    Study studyToStudyMapper(org.jaqpot.ambitclient.model.dto.study.Study study);

    Owner ownerToOwnerMapper(org.jaqpot.ambitclient.model.dto.study.Owner owner);

    Effect effectToEffectMapper(org.jaqpot.ambitclient.model.dto.study.Effect effect);

    Interpretation interpretationToInterpretationMapper(org.jaqpot.ambitclient.model.dto.study.Interpretation interpretation);

    Citation citationToCitationMapper(org.jaqpot.ambitclient.model.dto.study.Citation citation);

    Protocol protocolToProtocolMapper(org.jaqpot.ambitclient.model.dto.study.Protocol protocol);

    Company companyToCompanyMapper(org.jaqpot.ambitclient.model.dto.study.Company company);

    Category categoryToCategoryMapper(org.jaqpot.ambitclient.model.dto.study.Category category);

    Result resultToResultMapper(org.jaqpot.ambitclient.model.dto.study.Result result);

    Substance substanceToSubstanceMapper(org.jaqpot.ambitclient.model.dto.study.Substance substance);


}
