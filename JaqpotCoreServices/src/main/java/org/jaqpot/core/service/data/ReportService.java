/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Header;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.OutputStream;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import org.jaqpot.core.model.Report;

/**
 *
 * @author hampos
 */
@Named
@RequestScoped
public class ReportService {

    public void report2PDF(Report report, OutputStream os) {

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, os);
        } catch (DocumentException ex) {
            throw new InternalServerErrorException(ex);
        }

        document.open();
        report.getSingleCalculations().forEach((key, value) -> {
            try {
                document.add(new Header(key, value.toString()));
            } catch (DocumentException ex) {
                throw new InternalServerErrorException(ex);
            }
        });
        document.close();

    }
}
