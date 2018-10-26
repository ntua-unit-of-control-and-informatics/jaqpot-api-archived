package org.jaqpot.core.model.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jaqpot.core.model.*;
import org.jaqpot.core.model.util.ROG;

public class FeatureBuilder implements EntityBuilder<Feature> {

    private final Feature feature;

    public FeatureBuilder(final String id) {
        this.feature = new Feature();
        this.feature.setId(id);
    }

    public FeatureBuilder(final Feature a) {
        this.feature = new Feature(a);
    }

    public static FeatureBuilder builder(Feature feature) {
        return new FeatureBuilder(feature);
    }

    public static FeatureBuilder builder(String id) {
        return new FeatureBuilder(id);
    }

    public static FeatureBuilder builderRandomId() {
        return new FeatureBuilder("FTR" + new ROG(true).nextString(12));
    }

    @Override
    public  Feature build() {
        return this.feature;
    }

    public  FeatureBuilder addAdmissibleValues(Set<String> admissibleValues) {
        if (feature.getAdmissibleValues() == null) {
            feature.setAdmissibleValues(admissibleValues);
        }
        return this;
    }

    public FeatureBuilder addPredictedFor(String predictorFor) {
        feature.setPredictorFor(predictorFor);
        return this;
    }

    public FeatureBuilder  addUnits(String units) {
        feature.setUnits(units);
        return this;
    }

    private void initMeta() {
        if (feature.getMeta() == null) {
            feature.setMeta(new MetaInfo());
        }
    }

    public FeatureBuilder addTitles(String... titles) {
        if (titles == null) {
            return this;
        }
        initMeta();
        if (feature.getMeta().getTitles() == null) {
            feature.getMeta().setTitles(new HashSet<>(titles.length));
        }
        feature.getMeta().getTitles().addAll(Arrays.asList(titles));
        return this;
    }

    public FeatureBuilder addDescriptions(String... descriptions) {
        if (descriptions == null) {
            return this;
        }
        initMeta();
        if (feature.getMeta().getDescriptions() == null) {
            feature.getMeta().setDescriptions(new HashSet<>(descriptions.length));
        }
        feature.getMeta().getDescriptions().addAll(Arrays.asList(descriptions));
        return this;
    }

    public FeatureBuilder addSources(String... sources) {
        if (sources == null) {
            return this;
        }
        initMeta();
        if (feature.getMeta().getHasSources() == null) {
            feature.getMeta().setHasSources(new HashSet<>(sources.length));
        }
        feature.getMeta().getHasSources().addAll(Arrays.asList(sources));
        return this;
    }

    public FeatureBuilder addIdentifiers(String... identifiers) {
        if (identifiers == null) {
            return this;
        }
        initMeta();
        if (feature.getMeta().getIdentifiers() == null) {
            feature.getMeta().setIdentifiers(new HashSet<>(identifiers.length));
        }
        feature.getMeta().getIdentifiers().addAll(Arrays.asList(identifiers));
        return this;
    }

    public FeatureBuilder addTags(String... subjects) {
        if (subjects == null) {
            return this;
        }
        initMeta();
        if (feature.getMeta().getSubjects() == null) {
            feature.getMeta().setSubjects(new HashSet<>(subjects.length));
        }
        feature.getMeta().getSubjects().addAll(Arrays.asList(subjects));
        return this;
    }

    public FeatureBuilder addTagsCSV(String tagList) {
        if (tagList == null) {
            return this;
        }
        List<String> items = Arrays.asList(tagList.split("\\s*,\\s*"));
        items.forEach(s -> {
            items.set(items.indexOf(s), s.trim());
        });
        initMeta();
        if (feature.getMeta().getSubjects() == null) {
            feature.getMeta().setSubjects(new HashSet<>(items.size()));
        }
        feature.getMeta().getSubjects().addAll(items);
        return this;
    }

}