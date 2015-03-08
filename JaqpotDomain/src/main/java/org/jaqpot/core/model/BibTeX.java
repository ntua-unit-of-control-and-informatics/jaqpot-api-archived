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

    public BibTeX(BibTeX other) {
        super(other);
        this.address = other.address;
        this.annotation = other.annotation;
        this.author = other.author;
        this.bibTeXAbstract = other.bibTeXAbstract;
        this.bibType = other.bibType;
        this.bookTitle = other.bookTitle;
        this.chapter = other.chapter;
        this.copyright = other.copyright;
        this.createdBy = other.createdBy;
        this.crossref = other.crossref;
        this.edition = other.edition;
        this.editor = other.editor;
        this.isbn = other.isbn;
        this.issn = other.issn;
        this.journal = other.journal;
        this.key = other.key;
        this.keywords = other.keywords;
        this.number = other.number;
        this.pages = other.pages;
        this.series = other.series;
        this.title = other.title;
        this.url = other.url;
        this.volume = other.volume;
        this.year = other.year;
        this.school = other.school;
        this.publisher = other.publisher;
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
        

    private String bibTeXAbstract;
    /**
     * The name(s) of the author(s). In the case of more than one author,
     * separated by and.
     */
    private String author;
    /**
     * The title of the work.
     */
    private String title;
    /**
     * The title of the book, if only part of it is being cited.
     */
    private String bookTitle;
    /**
     * The school where the thesis was written.
     */
    private String school;
    /**
     * The chapter number
     */
    private String chapter;
    /**
     * Short copyright notice.
     */
    private String copyright;
    /**
     * The edition of a book, long form (such as "First" or "Second").
     */
    private String edition;
    /**
     * The name(s) of the editor(s).
     */
    private String editor;
    /**
     * The key of the cross-referenced entry.
     */
    private String crossref;
    /**
     * Publisher's address (usually just the city, but can be the full address
     * for lesser-known publishers).
     */
    private String address;
    /**
     * The year of publication (or, if unpublished, the year of creation).
     */
    private String year;
    /**
     * Page numbers, separated either by commas or double-hyphens.
     */
    private String pages;
    /**
     * The volume of a journal or multi-volume book.
     */
    private String volume;
    /**
     * The "(issue) number" of a journal, magazine, or tech-report, if
     * applicable. (Most publications have a "volume", but no "number" field.)
     */
    private String number;
    /**
     * The journal or magazine the work was published in.
     */
    private String journal;
    private String isbn;
    private String issn;
    private String keywords;
    /**
     * A hidden field used for specifying or overriding the alphabetical order
     * of entries (when the "author" and "editor" fields are missing). Note that
     * this is very different from the key (mentioned just after this list) that
     * is used to cite or cross-reference the entry.
     */
    private String key;
    /**
     * An annotation for annotated bibliography styles (not typical).
     */
    private String annotation;
    /**
     * The series of books the book was published in.
     */
    private String series;
    /**
     * The WWW address.
     */
    private String url;
    private BibTYPE bibType;
    private String createdBy;
    /**
     * The publisher's name.
     */
    private String publisher;

    @JsonProperty("abstract")
    public String getBibTeXAbstract() {
        return bibTeXAbstract;
    }

    @JsonProperty("abstract")
    public void setBibTeXAbstract(String bibTeXAbstract) {
        this.bibTeXAbstract = bibTeXAbstract;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
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
