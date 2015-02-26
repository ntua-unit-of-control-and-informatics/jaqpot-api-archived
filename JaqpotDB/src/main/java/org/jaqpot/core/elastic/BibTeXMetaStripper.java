/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.elastic;

import org.jaqpot.core.model.BibTeX;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class BibTeXMetaStripper extends AbstractMetaStripper<BibTeX> {

    public BibTeXMetaStripper(BibTeX entity) {
        super(entity);
    }

    @Override
    public BibTeX strip() {
        BibTeX bibtex = new BibTeX(entity);
        bibtex.setOntologicalClasses(null);
        return bibtex;
    }

    /**
     * This method will strip BibTeX entities from all that is potentially
     * unnecessary. It creates very small entities to be used as references in
     * other entities in ES.
     *
     * @return Ferociously stripped BibTeX object.
     */
    public BibTeX stripTease() {
        BibTeX bibtex = new BibTeX(entity);
        bibtex.setBibTeXAbstract(null);
        bibtex.setCopyright(null);
        bibtex.setCreatedBy(null);
        bibtex.setCrossref(null);
        bibtex.setMeta(null);
        bibtex.setPages(null);
        bibtex.setOntologicalClasses(null);
        return bibtex;
    }

}
