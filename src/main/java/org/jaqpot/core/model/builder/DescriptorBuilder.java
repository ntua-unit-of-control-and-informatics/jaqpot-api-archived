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

import org.jaqpot.core.model.*;
import org.jaqpot.core.model.util.ROG;

public class DescriptorBuilder implements EntityBuilder<Descriptor> {

    private final Descriptor descriptor;

    public DescriptorBuilder(final String id) {
        this.descriptor = new Descriptor();
        this.descriptor.setId(id);
    }

    public DescriptorBuilder(final Descriptor a) {
        this.descriptor = new Descriptor(a);
    }

    public static DescriptorBuilder builder(Descriptor descriptor) {
        return new DescriptorBuilder(descriptor);
    }

    public static DescriptorBuilder builder(String id) {
        return new DescriptorBuilder(id);
    }

    public static DescriptorBuilder builderRandomId() {
        return new DescriptorBuilder("ALG" + new ROG(true).nextString(14));
    }

    @Override
    public Descriptor build() {
        return this.descriptor;
    }

    public DescriptorBuilder setParameters(Set<Parameter> parameters) {
        descriptor.setParameters(parameters);
        return this;
    }

    public DescriptorBuilder setRanking(Integer ranking) {
        descriptor.setRanking(ranking);
        return this;
    }

    public DescriptorBuilder setBibtex(Set<BibTeX> bibtex) {
        descriptor.setBibtex(bibtex);
        return this;
    }

    public DescriptorBuilder setDescriptorService(String descriptorService) {
        descriptor.setDescriptorService(descriptorService);
        return this;
    }

    private void initMeta() {
        if (descriptor.getMeta() == null) {
            descriptor.setMeta(new MetaInfo());
        }
    }

    public DescriptorBuilder addTitles(String... titles) {
        if (titles == null) {
            return this;
        }
        initMeta();
        if (descriptor.getMeta().getTitles() == null) {
            descriptor.getMeta().setTitles(new HashSet<>(titles.length));
        }
        descriptor.getMeta().getTitles().addAll(Arrays.asList(titles));
        return this;
    }

    public DescriptorBuilder addDescriptions(String... descriptions) {
        if (descriptions == null) {
            return this;
        }
        initMeta();
        if (descriptor.getMeta().getDescriptions() == null) {
            descriptor.getMeta().setDescriptions(new HashSet<>(descriptions.length));
        }
        descriptor.getMeta().getDescriptions().addAll(Arrays.asList(descriptions));
        return this;
    }

    public DescriptorBuilder addTags(String... subjects) {
        if (subjects == null) {
            return this;
        }
        initMeta();
        if (descriptor.getMeta().getSubjects() == null) {
            descriptor.getMeta().setSubjects(new HashSet<>(subjects.length));
        }
        descriptor.getMeta().getSubjects().addAll(Arrays.asList(subjects));
        return this;
    }

    public DescriptorBuilder addTagsCSV(String tagList) {
        if (tagList == null) {
            return this;
        }
        List<String> items = Arrays.asList(tagList.split("\\s*,\\s*"));
        items.forEach(s -> {
            items.set(items.indexOf(s), s.trim());
        });
        initMeta();
        if (descriptor.getMeta().getSubjects() == null) {
            descriptor.getMeta().setSubjects(new HashSet<>(items.size()));
        }
        descriptor.getMeta().getSubjects().addAll(items);
        return this;
    }

}
