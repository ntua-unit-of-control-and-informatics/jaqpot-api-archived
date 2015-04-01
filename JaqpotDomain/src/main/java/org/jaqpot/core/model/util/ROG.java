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
package org.jaqpot.core.model.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.BibTeXBuilder;
import org.jaqpot.core.model.builder.MetaInfoBuilder;

/**
 * Random Object Generator for testing purposes mainly.
 *
 * @author Pantelis Sopasakis
 * @author Charalambos Chomenidis
 */
public class ROG {

    private final Random random;

    static final String AB = "0123456789ABabcdefgCDEFhijklmnoGHIJKpqrstuvLMNOPQRSwxyzTUVWXYZ";
    static Random rnd = new Random();
   

    public ROG(boolean secure) {
        random = !secure ? new Random() : new SecureRandom();
    }

    public String nextString(final int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    public MetaInfo nextMeta() {
        return MetaInfoBuilder.builder()
                .addAudiences(nextString(10), nextString(20))
                .addComments(nextString(10), nextString(10), nextString(10))
                .addContributors(nextString(10), nextString(50))
                .addCreators(nextString(10))
                .addDescriptions(nextString(2000))
                .addIdentifiers(nextString(20), nextString(10))
                .addPublishers(nextString(100))
                .addRights(nextString(2000))
                .addSameAs(nextString(10), nextString(10), nextString(10), nextString(10), nextString(10))
                .addSeeAlso(nextString(50))
                .addSources(nextString(10), nextString(10), nextString(10))
                .addSubjects(nextString(10), nextString(10), nextString(10), nextString(10), nextString(10))
                .addTitles(nextString(10), nextString(10))
                .setCurrentDate()
                .build();

    }

    public BibTeX nextBibTeX() {
        BibTeX bib = BibTeXBuilder.builder(nextString(10))
                .setAbstract(nextString(30))
                .setAddress(nextString(50))
                .setAnnotation(nextString(50))
                .setAuthor(nextString(50))
                .setBibType(BibTeX.BibTYPE.Article)
                .setBookTitle(nextString(50))                
                .setChapter(nextString(50))
                .setCopyright(nextString(50))
                .setCreatedBy(nextString(50))
                .setCrossref(nextString(50))
                .setEdition(nextString(50))
                .setEditor(nextString(50))
                .setISBN(nextString(50))
                .setISSN(nextString(50))
                .setJournal(nextString(50))
                .setKey(nextString(50))
                .setKeywords(nextString(50))
                .setNumber(nextString(50))
                .setPages(nextString(50))
                .setPublisher(nextString(10))
                .setSchool(nextString(10))
                .setSeries(nextString(50))
                .setTitle(nextString(50))
                .setURL(nextString(50))
                .setVolume(nextString(50))
                .setYear(nextString(50))
                .build();
        bib.setMeta(nextMeta());
        return bib;
    }

    public Set<String> nextSetString(int numberStrings, int stringLength) {
        Set<String> set = new HashSet<>(numberStrings);
        for (int i = 0; i < numberStrings; i++) {
            set.add(nextString(stringLength));
        }
        return set;
    }

    public List<String> nextListString(int numberStrings, int stringLength) {
        List<String> list = new ArrayList<>(numberStrings);
        for (int i = 0; i < numberStrings; i++) {
            list.add(nextString(stringLength));
        }
        return list;
    }

    public Parameter nextParameter() {
        Parameter p = new Parameter();
        p.setId(nextString(8));
        p.setMeta(nextMeta());
        p.setName(nextString(5));
        p.setOntologicalClasses(nextSetString(4, 10));
        p.setScope(Parameter.Scope.OPTIONAL);
        p.setValue(random.nextDouble());
        return p;
    }

    public Algorithm nextAlgorithm() {
        Algorithm a = new Algorithm(nextString(70));
        a.setBibtex(new HashSet<>());
        a.getBibtex().add(nextBibTeX());
        a.getBibtex().add(nextBibTeX());
        a.setCreatedBy(nextString(25));
        a.setMeta(nextMeta());
        a.setOntologicalClasses(nextSetString(10, 20));

        // parameters of the algorithms
        a.setParameters(new HashSet<>());
        for (int i = 0; i < 5; i++) {
            a.getParameters().add(nextParameter());
        }
        a.setRanking(10);
        return a;
    }

    public Model nextModel() {
        Model m = new Model(nextString(30));
        m.setActualModel(nextString(10000));
        m.setAlgorithm(nextAlgorithm());
        m.setBibtex(nextBibTeX());
        m.setCreatedBy(nextString(20));
        m.setDatasetUri(nextString(50));
        m.setDependentFeatures(nextListString(20, 5));
        m.setIndependentFeatures(nextListString(2, 5));
        m.setPredictedFeatures(nextListString(2, 10));
        m.setReliability(1);
        m.setMeta(nextMeta());
        m.setOntologicalClasses(nextSetString(5, 20));
        m.setParameters(new HashSet<>());
        for (int i = 0; i < 5; i++) {
            m.getParameters().add(nextParameter());
        }
        m.setPmmlModel(nextString(5000));
        m.setPmmlTransformations(nextString(10000));
        return m;
    }

    public Feature nextFeature() {
        Feature f = new Feature("/feature/" + nextString(5));
        f.setCreatedBy(nextString(10));
        f.setMeta(nextMeta());
        f.setOntologicalClasses(new HashSet<>());
        f.getOntologicalClasses().add("Feature");
        f.getOntologicalClasses().add("NumericFeature");
        f.setUnits("mg L^{-1}");

        return f;
    }

    public Conformer nextConformer() {
        Conformer c = new Conformer(nextString(10));
        c.setBibtex(nextBibTeX());
        c.setCreatedBy(nextString(20));
        c.setFatherCompound(nextString(5));

        // Features
        Map<String, FeatureValue> featureValues = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            FeatureValue fv = new FeatureValue("fv/" + i);
            fv.setFeature("feature/123");
            fv.setValue(random.nextInt());
            featureValues.put(fv.getFeature(), fv);
        }
        c.setFeatures(featureValues);

        // Predicted features
        Map<String, FeatureValue> predictedFeatureValues = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            FeatureValue fv = new FeatureValue("fv/" + i);
            fv.setFeature("feature/123");
            fv.setValue(random.nextInt());
            predictedFeatureValues.put(fv.getFeature(), fv);
        }
        c.setPredictedFeatures(predictedFeatureValues);

        // Chemical representations
        c.setRepresentations(new HashMap<>());
        c.getRepresentations().put("smiles", nextString(10));
        c.getRepresentations().put("inchi", nextString(20));
        c.getRepresentations().put("sdf", nextString(200));
        c.getRepresentations().put("mol", nextString(200));

        // Substructures
        c.setSubstructures(nextSetString(20, 5));

        c.setMeta(nextMeta());

        return c;
    }

}
