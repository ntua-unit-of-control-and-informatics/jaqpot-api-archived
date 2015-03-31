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
package org.jaqpot.core.model.builder;

import java.util.UUID;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class BibTeXBuilder implements EntityBuilder<BibTeX> {

    private final BibTeX bibtex;

    @Override
    public BibTeX build() {
        return this.bibtex;
    }

    /**
     * Created a BibTeX builder which will instantiate a new BibTeX object with
     * given ID.
     *
     * @param id
     * @return
     * @see #builderRandomUuid()
     */
    public static BibTeXBuilder builder(String id) {
        return new BibTeXBuilder(id);
    }

    public static BibTeXBuilder builder(BibTeX bibtex) {
        return new BibTeXBuilder(bibtex);
    }

    /**
     * Created a BibTeX builder which will instantiate a new BibTeX object with
     * a randomly generated ID in the form of a UUID.
     *
     * @return
     */
    public static BibTeXBuilder builderRandomUuid() {
        ROG rog = new ROG(true);
        return new BibTeXBuilder("BIB" + rog.nextString(12));
    }

    private BibTeXBuilder(String id) {
        bibtex = new BibTeX(id);
        bibtex.setId(id);
    }

    private BibTeXBuilder(BibTeX other) {
        this.bibtex = other;
    }

    public BibTeXBuilder setAddress(String address) {
        bibtex.setAddress(address);
        return this;
    }

    public BibTeXBuilder setTitle(String title) {
        bibtex.setTitle(title);
        return this;
    }

    public BibTeXBuilder setAnnotation(String annotation) {
        bibtex.setAnnotation(annotation);
        return this;
    }

    public BibTeXBuilder setAuthor(String author) {
        bibtex.setAuthor(author);
        return this;
    }

    public BibTeXBuilder setAbstract(String abstr) {
        bibtex.setBibTeXAbstract(abstr);
        return this;
    }

    public BibTeXBuilder setBibType(BibTeX.BibTYPE bibType) {
        bibtex.setBibType(bibType);
        return this;
    }

    public BibTeXBuilder setBookTitle(String title) {
        bibtex.setBookTitle(title);
        return this;
    }

    public BibTeXBuilder setChapter(String chapter) {
        bibtex.setChapter(chapter);
        return this;
    }

    public BibTeXBuilder setCopyright(String copyright) {
        bibtex.setCopyright(copyright);
        return this;
    }

    public BibTeXBuilder setCreatedBy(String title) {
        bibtex.setCreatedBy(title);
        return this;
    }

    public BibTeXBuilder setCrossref(String title) {
        bibtex.setCrossref(title);
        return this;
    }

    public BibTeXBuilder setEdition(String edition) {
        bibtex.setEdition(edition);
        return this;
    }

    public BibTeXBuilder setEditor(String editor) {
        bibtex.setEditor(editor);
        return this;
    }

    public BibTeXBuilder setISBN(String isbn) {
        bibtex.setIsbn(isbn);
        return this;
    }

    public BibTeXBuilder setISSN(String issn) {
        bibtex.setIssn(issn);
        return this;
    }

    public BibTeXBuilder setJournal(String journal) {
        bibtex.setJournal(journal);
        return this;
    }

    public BibTeXBuilder setKey(String key) {
        bibtex.setKey(key);
        return this;
    }

    public BibTeXBuilder setKeywords(String keywords) {
        bibtex.setKeywords(keywords);
        return this;
    }

    public BibTeXBuilder setNumber(String number) {
        bibtex.setNumber(number);
        return this;
    }

    public BibTeXBuilder setPages(String pages) {
        bibtex.setPages(pages);
        return this;
    }

    public BibTeXBuilder setSeries(String series) {
        bibtex.setSeries(series);
        return this;
    }

    public BibTeXBuilder setURL(String url) {
        bibtex.setUrl(url);
        return this;
    }

    public BibTeXBuilder setVolume(String volume) {
        bibtex.setVolume(volume);
        return this;
    }

    public BibTeXBuilder setSchool(String school) {
        bibtex.setSchool(school);
        return this;
    }

    public BibTeXBuilder setPublisher(String publisher) {
        bibtex.setPublisher(publisher);
        return this;
    }

    public BibTeXBuilder setYear(String year) {
        bibtex.setYear(year);
        return this;
    }

}
