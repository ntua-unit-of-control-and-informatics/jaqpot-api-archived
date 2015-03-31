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
package org.jaqpot.core.model.validator;

import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.builder.ErrorReportBuilder;

/**
 *
 * @author chung
 */
public class BibTeXValidator {
    
    

    /**
     *
     * @param bibtex BibTeX to be validated
     *
     * @return <code>null</code> if no problem has been detected, or an error
     * report otherwise.
     *
     * @throws NullPointerException in case the provided object is null.
     */
    public static ErrorReport validate(final BibTeX bibtex) {
        if (bibtex == null) {
            throw new NullPointerException();
        }
        
        ErrorReport error = null;
        
        if (bibtex.getTitle() == null){
            error = ErrorReportBuilder.builderRandomId()
                        .setActor("client")
                        .setCode("BibTeX::TitleMissing")
                        .setMessage("Every BibTeX entry must have a title")
                        .setHttpStatus(400)
                        .build();
        }
        
        
        if (bibtex.getBibType() == null) {
            error = ErrorReportBuilder.builderRandomId()
                    .setActor("client")
                    .setCode("BibTeX::noBibType")
                    .setDetails("Every BibTeX entity must define a BibTeX type")
                    .setMessage("BibType was not provided")
                    .setHttpStatus(400)
                    .build();
        }
        if (BibTeX.BibTYPE.Article.equals(bibtex.getBibType())) {
            if (bibtex.getAuthor() == null
                    || bibtex.getTitle() == null
                    || bibtex.getJournal() == null
                    || bibtex.getYear() == null) {
                error = ErrorReportBuilder.builderRandomId()
                        .setActor("client")
                        .setCode("BibTeX::malformedArticle")
                        .setMessage("Every article must define the fields: Author, Title, Journal, Year")
                        .setDetails("BibTeX specs - see http://nwalsh.com/tex/texhelp/bibtx-8.html")
                        .setHttpStatus(400)
                        .build();
            }
        }
        if (BibTeX.BibTYPE.Book.equals(bibtex.getBibType())) {
            if ( (bibtex.getAuthor() == null && bibtex.getEditor()== null)
                    || bibtex.getTitle() == null
                    || bibtex.getPublisher()== null
                    || bibtex.getYear() == null) {
                error = ErrorReportBuilder.builderRandomId()
                        .setActor("client")
                        .setCode("BibTeX::malformedBook")
                        .setMessage("Every book must define the fields: Author OR Editor, Title, Publisher, Year")
                        .setDetails("BibTeX specs - see http://nwalsh.com/tex/texhelp/bibtx-9.html")
                        .setHttpStatus(400)
                        .build();
            }
        }
        if (BibTeX.BibTYPE.Inproceedings.equals(bibtex.getBibType())) {
            if ( bibtex.getAuthor() == null
                    || bibtex.getTitle() == null
                    || bibtex.getBookTitle()== null
                    || bibtex.getYear() == null) {
                error = ErrorReportBuilder.builderRandomId()
                        .setActor("client")
                        .setCode("BibTeX::malformedInProceedings")
                        .setDetails("Every InProceedings must define the fields: Author, Title, Booktitle, Year")
                        .setMessage("BibTeX specs - see http://nwalsh.com/tex/texhelp/bibtx-14.html")
                        .setHttpStatus(400)
                        .build();
            }
        }        
        return error;
    }
}
