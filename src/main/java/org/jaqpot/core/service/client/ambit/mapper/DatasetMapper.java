package org.jaqpot.core.service.client.ambit.mapper;

import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.dto.dataset.EntryId;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */
@Mapper
public interface DatasetMapper {

    DatasetMapper INSTANCE = Mappers.getMapper( DatasetMapper.class );

    Dataset datasetToDataset  (org.jaqpot.ambitclient.model.dataset.Dataset dataset);

    DataEntry dataEntryToDataEntry(org.jaqpot.ambitclient.model.dataset.DataEntry dataEntry);

    EntryId substanceToSubstance (org.jaqpot.ambitclient.model.dataset.Substance Substance);

    MetaInfo metaInfosToMetaInfos(org.jaqpot.ambitclient.model.MetaInfo metaInfo);

    Set<FeatureInfo> featureInfosToFeatureInfos (Set<org.jaqpot.ambitclient.model.dataset.FeatureInfo> featureInfo);

    FeatureInfo featureInfoToFeatureInfo (org.jaqpot.ambitclient.model.dataset.FeatureInfo featureInfo);

    Set<Dataset.DescriptorCategory> descriptorCategorysToDescriptorCategorys(Set<org.jaqpot.ambitclient.model.dataset.Dataset.DescriptorCategory> descriptorCategory);

    Dataset.DescriptorCategory descriptorCategoryToDescriptorCategory(org.jaqpot.ambitclient.model.dataset.Dataset.DescriptorCategory descriptorCategory);

}
