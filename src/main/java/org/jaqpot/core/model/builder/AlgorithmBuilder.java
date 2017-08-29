/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Parameter;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class AlgorithmBuilder implements EntityBuilder<Algorithm> {

    private final Algorithm algorithm;

    public AlgorithmBuilder(final String id) {
        this.algorithm = new Algorithm();
        this.algorithm.setId(id);
    }

    public AlgorithmBuilder(final Algorithm a) {
        this.algorithm = new Algorithm(a);
    }

    public static AlgorithmBuilder builder(Algorithm algorithm) {
        return new AlgorithmBuilder(algorithm);
    }

    public static AlgorithmBuilder builder(String id) {
        return new AlgorithmBuilder(id);
    }

    public static AlgorithmBuilder builderRandomId() {
        return new AlgorithmBuilder("ALG" + new ROG(true).nextString(12));
    }

    @Override
    public Algorithm build() {
        return this.algorithm;
    }

    public AlgorithmBuilder setParameters(Set<Parameter> parameters) {
        algorithm.setParameters(parameters);
        return this;
    }

    public AlgorithmBuilder setRanking(Integer ranking) {
        algorithm.setRanking(ranking);
        return this;
    }

    public AlgorithmBuilder setBibtex(Set<BibTeX> bibtex) {
        algorithm.setBibtex(bibtex);
        return this;
    }

    public AlgorithmBuilder setTrainingService(String trainingService) {
        algorithm.setTrainingService(trainingService);
        return this;
    }

    public AlgorithmBuilder setPredictionService(String predictionService) {
        algorithm.setPredictionService(predictionService);
        return this;
    }

    public AlgorithmBuilder setReportService(String reportService) {
        algorithm.setReportService(reportService);
        return this;
    }

    private void initMeta() {
        if (algorithm.getMeta() == null) {
            algorithm.setMeta(new MetaInfo());
        }
    }

    public AlgorithmBuilder addTitles(String... titles) {
        if (titles == null) {
            return this;
        }
        initMeta();
        if (algorithm.getMeta().getTitles() == null) {
            algorithm.getMeta().setTitles(new HashSet<>(titles.length));
        }
        algorithm.getMeta().getTitles().addAll(Arrays.asList(titles));
        return this;
    }

    public AlgorithmBuilder addDescriptions(String... descriptions) {
        if (descriptions == null) {
            return this;
        }
        initMeta();
        if (algorithm.getMeta().getDescriptions() == null) {
            algorithm.getMeta().setDescriptions(new HashSet<>(descriptions.length));
        }
        algorithm.getMeta().getDescriptions().addAll(Arrays.asList(descriptions));
        return this;
    }

    public AlgorithmBuilder addTags(String... subjects) {
        if (subjects == null) {
            return this;
        }
        initMeta();
        if (algorithm.getMeta().getSubjects() == null) {
            algorithm.getMeta().setSubjects(new HashSet<>(subjects.length));
        }
        algorithm.getMeta().getSubjects().addAll(Arrays.asList(subjects));
        return this;
    }

    public AlgorithmBuilder addTagsCSV(String tagList) {
        if (tagList == null) {
            return this;
        }
        List<String> items = Arrays.asList(tagList.split("\\s*,\\s*"));
        items.forEach(s -> {
            items.set(items.indexOf(s), s.trim());
        });
        initMeta();
        if (algorithm.getMeta().getSubjects() == null) {
            algorithm.getMeta().setSubjects(new HashSet<>(items.size()));
        }
        algorithm.getMeta().getSubjects().addAll(items);
        return this;
    }

}
