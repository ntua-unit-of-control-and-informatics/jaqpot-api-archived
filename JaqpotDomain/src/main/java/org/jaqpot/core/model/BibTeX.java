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

import java.lang.reflect.Field;

/**
 *
 * @author KinkyDesign
 */
public class BibTeX extends JaqpotEntity {

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
    private String mAbstract;
    private String mAuthor;
    private String mTitle;
    private String mBookTitle;
    private String mChapter;
    private String mCopyright;
    private String mEdition;
    private String mEditor;
    private String mCrossref;
    private String mAddress;
    private String mYear;
    private String mPages;
    private String mVolume;
    private String mNumber;
    private String mJournal;
    private String mIsbn;
    private String mIssn;
    private String mKeywords;
    private String mKey;
    private String mAnnotation;
    private String mSeries;
    private String mUrl;
    private BibTYPE mBibType;
    private User mCreatedBy;


    /**
     * The user that created the BibTeX object.
     *
     * @return Instance of {@link User}
     */
    public User getCreatedBy() {
        return mCreatedBy;
    }

    /**
     * Sets the user who created the current BibTeX object.
     *
     * @param createdBy Instance of {@link User}
     */
    public void setCreatedBy(User createdBy) {
        this.mCreatedBy = createdBy;
    }

    /**
     * The abstract of the BibTeX object.
     *
     * @return
     */
    public String getAbstract() {
        return mAbstract;
    }

    /**
     * Setter method for the abstract.
     *
     * @param theAbstract The abstract as a String.
     * @return The current modifiable instance of BibTeX with the updated
     * abstract.
     */
    public BibTeX setAbstract(String theAbstract) {
        this.mAbstract = theAbstract;
        return this;
    }

    /**
     * The name(s) of the author(s), in the format described in the LaTeX book.
     *
     * @return Name(s) of authors as String.
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Setter method for the author(s).
     *
     * @param author The list of authors or the single author as a String.
     * @return The current modifiable instance of BibTeX with the updated
     * author.
     * @see #getAuthor() #getAuthor
     */
    public BibTeX setAuthor(String author) {
        this.mAuthor = author;
        return this;
    }

    /**
     * The bibliographic type.
     *
     * @return The bibliographic type of the current BibTeX object as an
     * instance of {@link BibTYPE }.
     */
    public BibTYPE getBibType() {
        return mBibType;
    }

    /**
     * Setter for the bibliographic type.
     *
     * @param bibType The bibliographic type you need to specify.
     * @return The current modifiable instance of BibTeX with the updated value
     * of bibtype.
     * @see #getBibType() getBibType()
     */
    public BibTeX setBibType(BibTYPE bibType) {
        this.mBibType = bibType;
        return this;
    }

    /**
     * Title of a book, part of which is being cited. See the LaTeX book for how
     * to type titles. For book entries, use the <code>title</code> field
     * instead.
     *
     * @return The <code>booktitle</code> as String.
     */
    public String getBookTitle() {
        return mBookTitle;
    }

    /**
     * Setter method for the bookTitle.
     *
     * @param bookTitle The bookTitle as a String.
     * @return The current modifiable instance of BibTeX with the updated
     * bookTitle.
     */
    public BibTeX setBookTitle(String bookTitle) {
        this.mBookTitle = bookTitle;
        return this;
    }

    /**
     * A chapter (or section or whatever) number.
     *
     * @return The chapter title/number as a String.
     */
    public String getChapter() {
        return mChapter;
    }

    /**
     * Setter method for the chapter of the BibTeX resource.
     *
     * @param chapter The chapter as a String.
     * @return The current modifiable instance of BibTeX with the updated
     * chapter parameter.
     */
    public BibTeX setChapter(String chapter) {
        this.mChapter = chapter;
        return this;
    }

    public String getCopyright() {
        return mCopyright;
    }

    /**
     * Setter method for the copyright notice of the BibTeX resource.
     *
     * @param copyright The copyright notice as a String.
     * @return The current modifiable instance of BibTeX with the updated
     * copyright parameter.
     */
    public BibTeX setCopyright(String copyright) {
        this.mCopyright = copyright;
        return this;
    }

    /**
     * The database key of the entry being cross referenced. A URI of some other
     * BibTeX resource should be ideal for a cross-reference.
     *
     * @return Cross-reference to some other BibTeX resource or piece of work.
     */
    public String getCrossref() {
        return mCrossref;
    }

    /**
     * Setter method for the cross-ref of this BibTeX resource. A URI of some
     * other BibTeX entity should be ideally provided.
     *
     * @param crossref The chapter as a String.
     * @return The current modifiable instance of BibTeX with the updated
     * cross-ref.
     */
    public BibTeX setCrossref(String crossref) {
        this.mCrossref = crossref;
        return this;
    }

    /**
     * The edition of a book—for example, "Second". This should be an ordinal,
     * and should have the first letter capitalized, as shown here; the standard
     * styles convert to lower case when necessary.
     *
     * @return The edition of the BibTeX object.
     */
    public String getEdition() {
        return mEdition;
    }

    /**
     * Setter for the edition.
     *
     * @param edition
     * @return The current modifiable BibTeX object updated with the value of
     * edition.
     * @see #getEdition()
     */
    public BibTeX setEdition(String edition) {
        this.mEdition = edition;
        return this;
    }

    /**
     * Name(s) of editor(s), typed as indicated in the LaTeX book. If there is
     * also an author field, then the editor field gives the editor of the book
     * or collection in which the reference appears.
     *
     * @return Editor(s) as String.
     */
    public String getEditor() {
        return mEditor;
    }

    /**
     * Setter for the editor(s).
     *
     * @param editor
     * 
     * @return The current modifiable BibTeX object updated with the value of
     * editor(s).
     * 
     * @see #getEditor()
     */
    public BibTeX setEditor(String editor) {
        this.mEditor = editor;
        return this;
    }

    /**
     * The ISBN of the resource. ISBNs now come in two styles, containing 10
     * digits or 13 digits, respectively (corresponding to the above "ISBN-10:"
     * and "ISBN-13:" numbers). Please use the 13-digit one if available (if
     * nowhere else, it is written under the barcode: the hyphenation will be
     * 978-, or in the future 979-, then the same as in the 10-digit ISBN, but
     * the last digit is different for ISBN-10 and ISBN-13, as they use
     * different checksum algorithms).
     *
     * @return The ISBN of the current BibTeX object as a String.
     */
    public String getIsbn() {
        return mIsbn;
    }

    /**
     * Setter method for the ISBN.
     *
     * @param isbn The ISBN.
     * @return The current BibTeX instance with updated ISBN.
     * @see #getIsbn() #getIsbn()
     */
    public BibTeX setIsbn(String isbn) {
        this.mIsbn = isbn;
        return this;
    }

    /**
     * The ISSN of the resource. An International Standard Serial Number (ISSN)
     * is a unique eight-digit number used to identify a print or electronic
     * periodical publication. Periodicals published in both print and
     * electronic form may have two ISSNs, a print ISSN (p-ISSN) and an
     * electronic ISSN (e-ISSN or eISSN). The ISSN system was first drafted as
     * an ISO international standard in 1971 and published as ISO 3297 in 1975.
     * The ISO subcommittee TC 46/SC 9 is responsible for the standard.
     *
     * @return The ISSN of the current BibTeX object as a String.
     */
    public String getIssn() {
        return mIssn;
    }

    /**
     * Setter method for the ISSN.
     *
     * @param issn The ISSN.
     * @return The current BibTeX instance with updated ISSN.
     * @see #getIssn() #getIssn()
     */
    public BibTeX setIssn(String issn) {
        this.mIssn = issn;
        return this;
    }

    /**
     * A journal name. Abbreviations are provided for many journals; see the
     * Local Guide.
     *
     * @return The journal name as a String.
     */
    public String getJournal() {
        return mJournal;
    }

    /**
     * Setter for the journal name.
     *
     * @param journal Name of the journal.
     * @return The current BibTeX instance with updated <code>journal</code>.
     * @see #getJournal() getJournal()
     */
    public BibTeX setJournal(String journal) {
        this.mJournal = journal;
        return this;
    }

    /**
     * Used for alphabetizing, cross referencing, and creating a label when the
     * "author" information (described in Section 4) is missing. This field
     * should not be confused with the key that appears in the
     * <code>\cite</code> command and at the beginning of the database entry.
     *
     * @return
     */
    public String getKey() {
        return mKey;
    }

    /**
     * Setter for the variable key.
     *
     * @param key The key.
     * @return The current object with updated value of key.
     * @see #getKey() getKey()
     */
    public BibTeX setKey(String key) {
        this.mKey = key;
        return this;
    }

    /**
     * A list of keywords separated by any custom delimiter.
     *
     * @return List of keywords as a single string.
     */
    public String getKeywords() {
        return mKeywords;
    }

    /**
     * Set a list of keywords separated by any custom delimiter.
     *
     * @param keywords Keywords.
     * @return The current object with updated value of keywords.
     */
    public BibTeX setKeywords(String keywords) {
        this.mKeywords = keywords;
        return this;
    }

    /**
     * The number of a journal, magazine, technical report, or of a work in a
     * series. An issue of a journal or magazine is usually identified by its
     * volume and number; the organization that issues a technical report
     * usually gives it a number; and sometimes books are given numbers in a
     * named series.
     *
     * @return The number of the current BibTeX object.
     */
    public Integer getNumber() {
        if (mNumber == null) {
            return null;
        }
        return Integer.parseInt(mNumber);
    }

    /**
     * Setter method for the number of the current BibTeX object.
     *
     * @param number The <code>number</code> as Integer.
     * @return The current modifiable BibTeX object updated number.
     * @see #getNumber() getNumber()
     */
    public BibTeX setNumber(Integer number) {
        if (number == null || number < 0) {
            this.mNumber = null;
        } else {
            this.mNumber = Integer.toString(number);
        }
        return this;
    }

    /**
     * One or more page numbers or range of numbers, such as 42--111 or
     * 7,41,73--97 or 43+ (the ‘+’ in this last example indicates pages
     * following that don’t form a simple range). To make it easier to maintain
     * Scribe- compatible databases, the standard styles convert a single dash
     * (as in 7-33) to the double dash used in TEX to denote number ranges (as
     * in 7--33).
     *
     * @return Pages as String.
     */
    public String getPages() {
        return mPages;
    }

    /**
     * Setter method for the pages of the current BibTeX object.
     *
     * @param pages Pages as String
     * @return The current modifiable BibTeX object updated pages.
     * @see #getPages() getPages()
     */
    public BibTeX setPages(String pages) {
        this.mPages = pages;
        return this;
    }

    /**
     * The volume of a journal or multivolume book.
     *
     * @return The volume as Integer.
     */
    public Integer getVolume() {
        if (mVolume == null) {
            return null;
        }
        return Integer.parseInt(mVolume);
    }

    /**
     * Setter method for the volume of a journal or multivolume book.
     *
     * @param volume The volume as Integer.
     * @return The current modifiable BibTeX object updated volume.
     */
    public BibTeX setVolume(Integer volume) {
        if (volume == null || volume < 0) {
            this.mVolume = null;
        } else {
            this.mVolume = Integer.toString(volume);
        }
        return this;
    }

    /**
     * The year of publication or, for an unpublished work, the year it was
     * written. Generally it should consist of four numerals, such as
     * <code>1984</code>. This method returns the year as Integer.
     *
     * @return The year of publication/inception as Integer.
     */
    public Integer getYear() {
        if (mYear == null) {
            return null;
        }
        return Integer.parseInt(mYear);
    }

    /**
     * Set the year of publication. The year is provided as an instance of
     * Integer. If the supplied value is <code>null</code>, or not-null but
     * non-positive, then no value is stored in the corresponding field.
     *
     * @param year The year of publication for the BibTeX provided as an Integer
     * object.
     * @return The current updated BibTeX object.
     */
    public BibTeX setYear(Integer year) {
        if (year == null || (year != null && year < 0)) {
            this.mYear = null;
        } else {
            this.mYear = Integer.toString(year);
        }
        return this;
    }

    /**
     * Usually the address of the publisher or other type of institution. For
     * major publishing houses, van Leunen recommends omitting the information
     * entirely. For small publishers, on the other hand, you can help the
     * reader by giving the complete address.
     *
     * @return The address as String.
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * Setter method for the address of the publisher or other type of
     * institution. For major publishing houses, it is preferable to omit the
     * information entirely. For other smaller publishers, you can help the
     * reader by giving the complete address.
     *
     * @param address The address as a simple String.
     * @return The current modifiable BibTeX object updated address.
     */
    public BibTeX setAddress(String address) {
        this.mAddress = address;
        return this;
    }

    /**
     * An annotation. It is not used by the standard bibliography styles, but
     * may be used by others that produce an annotated bibliography.
     *
     * @return Annotation as String.
     */
    public String getAnnotation() {
        return mAnnotation;
    }

    /**
     * Setter method for the annotation.
     *
     * @param annotation The annotation as String.
     * @return The current modifiable BibTeX object updated annotation.
     * @see #getAnnotation() getAnnotation()
     */
    public BibTeX setAnnotation(String annotation) {
        this.mAnnotation = annotation;
        return this;
    }

    /**
     * The name of a series or set of books. When citing an entire book, the the
     * title field gives its title and an optional series field gives the name
     * of a series or multi-volume set in which the book is published.
     *
     * @return The <code>series</code> as String.
     */
    public String getSeries() {
        return mSeries;
    }

    /**
     * Setter method for the <code>series</code> of the BibTeX entry.
     *
     * @param series Series as String.
     * @return The current modifiable BibTeX object updated series.
     */
    public BibTeX setSeries(String series) {
        this.mSeries = series;
        return this;
    }

    /**
     * The work’s title, typed as explained in the LaTeX book.
     *
     * @return The title as String.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Setter method for the title.
     *
     * @param title The title as String.
     * @return The current modifiable BibTeX object updated title.
     */
    public BibTeX setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    /**
     * A URL where more information about the work described by this BibTeX can
     * be found. Usually, it is the URL where someone may find the actual
     * resource.
     *
     * @return URL as String.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     *
     * @param url The URL of the BibTeX actual document.
     * @return The current modifiable BibTeX object updated annotation.
     */
    public BibTeX setUrl(String url) {
        this.mUrl = url;
        return this;
    }// </editor-fold>
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("@");
        result.append(mBibType.toString());
        result.append("{");
        result.append(getId());
        result.append(",\n");
        result.append("author = \"");
        result.append(mAuthor);
        result.append("\"");

        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getName().equals("mCreatedBy")
                        && !f.getName().equals("mAuthor")
                        && !f.getName().equals("mBibType")
                        && !f.getName().equals("logger")
                        && f.get(this) != null) {
                    result.append(",\n");
                    result.append(f.getName().substring(1));
                    result.append(" = \"");
                    result.append(f.get(this));
                    result.append("\"");
                }
            } catch (final IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        result.append("\n}\n");
        return result.toString();
    }
}

