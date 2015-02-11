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
package org.jaqpot.core.model;

import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BibTeX extends JaqpotEntity {

    public BibTeX() {
    }

    public BibTeX(String id) {
        super(id);
    }       

    /**
     * Enumeration for bibliographic types supported by the Knouf ontology.
     */
    public enum BibTYPE {

        /**
         * An article from a journal or magazine. Required fields: author,
         * title, journal, year. Optional fields: volume, number, pages, month,
         * note.
         */
        Article,
        /**
         * A book with an explicit publisher. Required fields: author or editor,
         * title, publisher, year. Optional fields: volume or number, series,
         * address, edition, month, note.
         */
        Book,
        /**
         * An oral presentation or a poster presented in a conference or
         * document that was presented in a conference by any means. Symposium,
         * workshops and other similar presentations are also reported here.
         */
        Conference,
        /**
         * A PhD dissertation. Required fields: author, title, school, year.
         * Optional fields: type, address, month, note.
         */
        Phdthesis,
        /**
         * A work that is printed and bound, but without a named publisher or
         * sponsoring institution. Required field: title. Optional fields:
         * author, howpublished, address, month, year, note.
         */
        Booklet,
        /**
         * A part of a book, which may be a chapter (or section or whatever)
         * and/or a range of pages. Required fields: author or editor, title,
         * chapter and/or pages, publisher, year. Optional fields: volume or
         * number, series, type, address, edition, month, note.
         */
        Inbook,
        /**
         * A part of a book having its own title. Required fields: author,
         * title, booktitle, publisher, year. Optional fields: editor, volume or
         * number, series, type, chapter, pages, address, edition, month, note.
         */
        Incollection,
        /**
         * An article in a conference proceedings.
         */
        Inproceedings,
        /**
         * Technical documentation
         */
        Manual,
        /**
         * A Master's thesis.
         */
        Mastersthesis,
        /**
         * Misc, Use this type when nothing else fits.
         */
        Misc,
        /**
         * The proceedings of a conference.
         */
        Proceedings,
        /**
         * A report published by a school or other institution, usually numbered
         * within a series.
         */
        TechReport,
        /**
         * A document having an author and title, but not formally published.
         */
        Unpublished,
        /**
         * A generic BibTeX entry which encapsulates all cases.
         */
        Entry;
    }
    /*
     * WARNING: DO NOT MODIFY THE NAMES OF THE FOLLOWING FIELDS
     * BECAUSE SOME METHODS IN BIBTEX USE REFLECTIVE LOOKUPS AND COMPARISONS
     * BASED ON THE NAME OF THE FIELD.
     */
    private String bibTeXAbstract;
    private String author;
    private String title;
    private String bookTitle;
    private String chapter;
    private String copyright;
    private String edition;
    private String editor;
    private String crossref;
    private String address;
    private String year;
    private String pages;
    private String volume;
    private String number;
    private String journal;
    private String isbn;
    private String issn;
    private String keywords;
    private String key;
    private String annotation;
    private String series;
    private String url;
    private BibTYPE bibType;
    private String createdBy;

     @JsonProperty("abstract")
    public String getBibTeXAbstract() {
        return bibTeXAbstract;
    }

    public void setBibTeXAbstract(String bibTeXAbstract) {
        this.bibTeXAbstract = bibTeXAbstract;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getCrossref() {
        return crossref;
    }

    public void setCrossref(String crossref) {
        this.crossref = crossref;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BibTYPE getBibType() {
        return bibType;
    }

    public void setBibType(BibTYPE bibType) {
        this.bibType = bibType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    

}
