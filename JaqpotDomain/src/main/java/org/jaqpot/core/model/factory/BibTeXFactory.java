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
package org.jaqpot.core.model.factory;

import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.builder.BibTeXBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalambos Chomenidis
 */
public class BibTeXFactory {

    public static BibTeX article(
            String id,
            String title,
            String authors,
            String journal,
            int year) {
        return BibTeXBuilder.builder(id).
                setBibType(BibTeX.BibTYPE.Article).
                setTitle(title).
                setAuthor(authors).
                setJournal(journal).
                setYear(Integer.toString(year)).
                build();
    }

    public static BibTeX inProceedings(
            String id,
            String title,
            String authors,
            String booktitle,
            int year) {
        return BibTeXBuilder.builder(id).
                setBibType(BibTeX.BibTYPE.Inproceedings).
                setTitle(title).
                setAuthor(authors).
                setBookTitle(booktitle).
                setYear(Integer.toString(year)).
                build();
    }

    public static BibTeX phdThesis(
            String id,
            String title,
            String authors,
            String school,
            int year) {
        return BibTeXBuilder.builder(id).
                setBibType(BibTeX.BibTYPE.Phdthesis).
                setTitle(title).
                setAuthor(authors).
                setSchool(school).
                setYear(Integer.toString(year)).
                build();
    }

    public static BibTeX book(
            String id,
            String title,
            String authors,
            String publisher,
            String address,
            String series,
            int year) {
        return BibTeXBuilder.builder(id).
                setBibType(BibTeX.BibTYPE.Book).
                setTitle(title).
                setAuthor(authors).
                setPublisher(publisher).
                setSeries(series).
                setAddress(address).
                setYear(Integer.toString(year)).
                build();
    }

}
