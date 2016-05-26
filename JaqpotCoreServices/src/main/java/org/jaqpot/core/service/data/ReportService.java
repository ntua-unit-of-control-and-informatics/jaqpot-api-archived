/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Header;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import org.jaqpot.core.model.ArrayCalculation;
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
        Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
        Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Font tableFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

        Chunk chunk = new Chunk("Report", chapterFont);
        Chapter chapter = new Chapter(new Paragraph(chunk), 1);
        chapter.setNumberDepth(0);

        report.getSingleCalculations().forEach((key, value) -> {

            chapter.add(new Paragraph(key + ": " + value.toString().trim().replaceAll(" +", " "), paragraphFont));

        });

        try {
            document.add(chapter);
        } catch (DocumentException ex) {
            throw new InternalServerErrorException(ex);
        }

        for (Entry<String, ArrayCalculation> entry : report.getArrayCalculations().entrySet()) {
            String label = entry.getKey();
            ArrayCalculation ac = entry.getValue();

            PdfPTable table = new PdfPTable(ac.getColNames().size() + 1);

            for (Entry<String, List<Object>> row : ac.getValues().entrySet()) {
                table.addCell(row.getKey());
                for (Object o : row.getValue()) {
                    table.addCell(o.toString());
                }
                table.completeRow();
            }
            try {
                Chunk tableChunk = new Chunk(label, tableFont);
                Chapter tableChapter = new Chapter(new Paragraph(tableChunk), 1);
                tableChapter.add(table);
                document.add(tableChapter);
            } catch (DocumentException ex) {
                throw new InternalServerErrorException(ex);
            }
        }

        document.close();
    }
}
