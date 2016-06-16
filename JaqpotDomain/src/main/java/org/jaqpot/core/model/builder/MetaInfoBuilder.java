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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.jaqpot.core.model.MetaInfo;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class MetaInfoBuilder implements EntityBuilder<MetaInfo> {

    private final MetaInfo meta;

    @Override
    public MetaInfo build() {
        return this.meta;
    }

    public static MetaInfoBuilder builder() {
        return new MetaInfoBuilder();
    }

    public static MetaInfoBuilder builder(MetaInfo other) {
        return new MetaInfoBuilder(other);
    }

    private MetaInfoBuilder() {
        meta = new MetaInfo();
        meta.setDate(new Date());
    }

    private MetaInfoBuilder(MetaInfo meta) {
        this.meta = meta;
    }

    public MetaInfoBuilder addTitles(String... titles) {
        if (titles == null) {
            return this;
        }
        if (meta.getTitles() == null) {
            meta.setTitles(new HashSet<>());
        }
        meta.getTitles().addAll(Arrays.asList(titles));
        return this;
    }

    public MetaInfoBuilder addAudiences(String... audiences) {
        if (audiences == null) {
            return this;
        }
        if (meta.getAudiences() == null) {
            meta.setAudiences(new HashSet<>());
        }
        meta.getAudiences().addAll(Arrays.asList(audiences));
        return this;
    }

    public MetaInfoBuilder addComments(String... comments) {
        if (comments == null) {
            return this;
        }
        if (meta.getComments() == null) {
            meta.setComments(new ArrayList<>());
        }
        meta.getComments().addAll(Arrays.asList(comments));
        return this;
    }

    public MetaInfoBuilder addContributors(String... contributors) {
        if (contributors == null) {
            return this;
        }
        if (meta.getContributors() == null) {
            meta.setContributors(new HashSet<>());
        }
        meta.getContributors().addAll(Arrays.asList(contributors));
        return this;
    }

    public MetaInfoBuilder addCreators(String... creators) {
        if (creators == null) {
            return this;
        }
        if (meta.getCreators() == null) {
            meta.setCreators(new HashSet<>());
        }
        meta.getCreators().addAll(Arrays.asList(creators));
        return this;
    }

    public MetaInfoBuilder addCreators(Set<String> creators) {
        if (creators == null) {
            return this;
        }
        if (meta.getCreators() == null) {
            meta.setCreators(new HashSet<>());
        }
        meta.getCreators().addAll(creators);
        return this;
    }

    public MetaInfoBuilder addDescriptions(String... descriptions) {
        if (descriptions == null) {
            return this;
        }
        if (meta.getDescriptions() == null) {
            meta.setDescriptions(new HashSet<>());
        }
        meta.getDescriptions().addAll(Arrays.asList(descriptions));
        return this;
    }

    public MetaInfoBuilder addSources(String... sources) {
        if (sources == null) {
            return this;
        }
        if (meta.getHasSources() == null) {
            meta.setHasSources(new HashSet<>());
        }
        meta.getHasSources().addAll(Arrays.asList(sources));
        return this;
    }

    public MetaInfoBuilder addIdentifiers(String... ids) {
        if (ids == null) {
            return this;
        }
        if (meta.getIdentifiers() == null) {
            meta.setIdentifiers(new HashSet<>());
        }
        meta.getIdentifiers().addAll(Arrays.asList(ids));
        return this;
    }

    public MetaInfoBuilder addPublishers(String... publishers) {
        if (publishers == null) {
            return this;
        }
        if (meta.getPublishers() == null) {
            meta.setPublishers(new HashSet<>());
        }
        meta.getPublishers().addAll(Arrays.asList(publishers));
        return this;
    }

    public MetaInfoBuilder addRights(String... rights) {
        if (rights == null) {
            return this;
        }
        if (meta.getRights() == null) {
            meta.setRights(new HashSet<>());
        }
        meta.getRights().addAll(Arrays.asList(rights));
        return this;
    }

    public MetaInfoBuilder addSameAs(String... sameAs) {
        if (sameAs == null) {
            return this;
        }
        if (meta.getSameAs() == null) {
            meta.setSameAs(new HashSet<>());
        }
        meta.getSameAs().addAll(Arrays.asList(sameAs));
        return this;
    }

    public MetaInfoBuilder addSeeAlso(String... seeAlso) {
        if (seeAlso == null) {
            return this;
        }
        if (meta.getSeeAlso() == null) {
            meta.setSeeAlso(new HashSet<>());
        }
        meta.getSeeAlso().addAll(Arrays.asList(seeAlso));
        return this;
    }

    public MetaInfoBuilder addSubjects(String... subjects) {
        if (subjects == null) {
            return this;
        }
        if (meta.getSubjects() == null) {
            meta.setSubjects(new HashSet<>());
        }
        meta.getSubjects().addAll(Arrays.asList(subjects));
        return this;
    }

    public MetaInfoBuilder setDate(Date date) {
        meta.setDate(date);
        return this;
    }

    public MetaInfoBuilder setCurrentDate() {
        meta.setDate(new Date());
        return this;
    }

    public MetaInfoBuilder setTotalColumns(Integer totalColumns) {
        meta.setTotalColumns(totalColumns);
        return this;
    }

    public MetaInfoBuilder getTotalRows(Integer totalRows) {
        meta.setTotalRows(totalRows);
        return this;
    }
}
