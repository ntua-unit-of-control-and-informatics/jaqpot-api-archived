/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.core.model.ld.mappers;

import java.util.ArrayList;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.ld.BaseLD;

/**
 *
 * @author Charalampos Chomenidis
 * @author Angelos Valsamis
 * 
 */
public class BaseMapper {

    public BaseLD mapMeta(MetaInfo meta, BaseLD base) {
        if (meta != null) {
            if (meta.getAudiences() != null) {
                base.setAudience(new ArrayList<>(meta.getAudiences()));
            }
            if (meta.getComments() != null) {
                base.setComment(new ArrayList<>(meta.getComments()));
            }
            if (meta.getContributors() != null) {
                base.setContributor(new ArrayList<>(meta.getContributors()));
            }
            if (meta.getCreators() != null) {
                base.setCreator(new ArrayList<>(meta.getCreators()));
            }
            if (meta.getDate() != null) {
                base.setDate(meta.getDate().toString());
            }
            if (meta.getDescriptions() != null) {
                base.setDescription(new ArrayList<>(meta.getDescriptions()));
            }
            if (meta.getDoi() != null) {
                base.setDoi(meta.getDoi().stream().findFirst().orElse(null));
            }
            if (meta.getHasSources() != null) {
                base.setSource(new ArrayList<>(meta.getHasSources()));
            }
            if (meta.getPublishers() != null) {
                base.setPublisher(new ArrayList<>(meta.getPublishers()));
            }
            if (meta.getRights() != null) {
                base.setRights(new ArrayList<>(meta.getRights()));
            }
            if (meta.getSameAs() != null) {
                base.setSameAs(new ArrayList<>(meta.getSameAs()));
            }
            if (meta.getSeeAlso() != null) {
                base.setSeeAlso(new ArrayList<>(meta.getSeeAlso()));
            }
            if (meta.getSubjects() != null) {
                base.setSubject(new ArrayList<>(meta.getSubjects()));
            }
            if (meta.getTitles() != null) {
                base.setTitle(new ArrayList<>(meta.getTitles()));
            }
        }
        return base;
    }
}
