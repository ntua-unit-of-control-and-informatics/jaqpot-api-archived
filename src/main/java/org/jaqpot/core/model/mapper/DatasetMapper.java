package org.jaqpot.core.model.mapper;

import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */
@Mapper
public interface DatasetMapper {

    DatasetMapper INSTANCE = Mappers.getMapper( DatasetMapper.class );

    Dataset datasetToDataset  (org.jaqpot.ambitclient.model.dataset.Dataset dataset);

    org.jaqpot.core.model.MetaInfo metaInfoToMetaInfo(org.jaqpot.ambitclient.model.MetaInfo metaInfo);

    org.jaqpot.core.model.dto.dataset.Substance substanceToSubstance(org.jaqpot.ambitclient.model.dataset.Substance substance);

    DataEntry dataEntryToDataEntry(org.jaqpot.ambitclient.model.dataset.DataEntry dataEntry);

    FeatureInfo featureInfoToFeatureInfo (org.jaqpot.ambitclient.model.dataset.FeatureInfo featureInfo);

    Dataset.DescriptorCategory descriptorCategoryToDescriptorCategory(org.jaqpot.ambitclient.model.dataset.Dataset.DescriptorCategory descriptorCategory);
}
