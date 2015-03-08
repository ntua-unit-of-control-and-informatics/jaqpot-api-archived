/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        if (bibtex.getBibType() == null) {
            error = ErrorReportBuilder.builderRandomUuid()
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
                error = ErrorReportBuilder.builderRandomUuid()
                        .setActor("client")
                        .setCode("BibTeX::malformedArticle")
                        .setDetails("Every article must define the fields: Author, Title, Journal, Year")
                        .setMessage("BibTeX specs - see http://nwalsh.com/tex/texhelp/bibtx-8.html")
                        .setHttpStatus(400)
                        .build();
            }
        }
        if (BibTeX.BibTYPE.Book.equals(bibtex.getBibType())) {
            if ( (bibtex.getAuthor() == null && bibtex.getEditor()== null)
                    || bibtex.getTitle() == null
                    || bibtex.getPublisher()== null
                    || bibtex.getYear() == null) {
                error = ErrorReportBuilder.builderRandomUuid()
                        .setActor("client")
                        .setCode("BibTeX::malformedBook")
                        .setDetails("Every book must define the fields: Author OR Editor, Title, Publisher, Year")
                        .setMessage("BibTeX specs - see http://nwalsh.com/tex/texhelp/bibtx-9.html")
                        .setHttpStatus(400)
                        .build();
            }
        }
        if (BibTeX.BibTYPE.Inproceedings.equals(bibtex.getBibType())) {
            if ( bibtex.getAuthor() == null
                    || bibtex.getTitle() == null
                    || bibtex.getBookTitle()== null
                    || bibtex.getYear() == null) {
                error = ErrorReportBuilder.builderRandomUuid()
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
