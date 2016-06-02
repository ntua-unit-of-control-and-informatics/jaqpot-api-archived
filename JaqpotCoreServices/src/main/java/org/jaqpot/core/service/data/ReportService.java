/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import com.itextpdf.text.*;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.*;
import org.apache.commons.codec.binary.Base64;
import org.jaqpot.core.model.ArrayCalculation;
import org.jaqpot.core.model.Report;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Angelos Valsamis
 * @author hampos
 */
@Named
@RequestScoped
public class ReportService {

    /** helper function to add logo image in header*/
    public static PdfPCell createImageCell(String path) throws DocumentException, IOException {
        Image img = Image.getInstance(path);
        img.scaleToFit(120f,120f);
        PdfPCell cell = new PdfPCell(img, false);
        cell.setPaddingBottom(5f);
        cell.setUseBorderPadding(true);
        cell.setBorder(Rectangle.BOTTOM);
        return cell;
    }

    /**
     * Inner class to add a table as header.
     */
    class TableHeader extends PdfPageEventHelper {
        /** The header text. */
        String header;

        String logo = "eNanoMapper Report Service";

        String img = "http://www.enanomapper.net/sites/all/themes/theme807/logo.png";

        /** The template with the total number of pages. */
        PdfTemplate total;

        /**
         * Allows us to change the content of the header.
         * @param header The new header String
         */
        public void setHeader(String header) {
            this.header = header;
        }

        /**
         * Creates the PdfTemplate that will hold the total number of pages.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        /**
         * Adds a header to every page
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable table = new PdfPTable(4);
            try {

                /**PdfTable as header*/
                table.setWidths(new int[]{logo.length(),24, 24, 2});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(20);
                table.getDefaultCell().setBorder(Rectangle.BOTTOM);

                try {
                    table.addCell(createImageCell(img));
                } catch (IOException e) {
                    table.addCell(logo);
                    e.printStackTrace();
                }
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(header);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(String.format("Page %d of", writer.getPageNumber()));
                PdfPCell cell = new PdfPCell(Image.getInstance(total));
                cell.setBorder(Rectangle.BOTTOM);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());

                /**Phrase as footer*/
                PdfContentByte cb = writer.getDirectContent();
                Font ffont = new Font(Font.FontFamily.UNDEFINED, 12, Font.ITALIC);

                Phrase footer = new Phrase(" - JaqpotQuatro 2016 - ", ffont);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                        footer,
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 10, 0);
            }
            catch(DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        /**
         * Fills out the total number of pages before the document is closed.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber())),
                    2, 2, 0);
        }
    }

    public void report2PDF(Report report, OutputStream os) {

        Document document = new Document();
        document.setPageSize(PageSize.A4);
        document.setMargins(50, 45, 80, 40);
        document.setMarginMirroring(false);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, os);
            TableHeader event = new TableHeader();
            writer.setPageEvent(event);

        } catch (DocumentException ex) {
            throw new InternalServerErrorException(ex);
        }

        document.open();

        /** setup fonts for pdf */
        Font ffont = new Font(Font.FontFamily.UNDEFINED, 9, Font.ITALIC);
        Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
        Font paragraphFontBold = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Font tableFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

        /** print link to jaqpot*/
        Chunk chunk = new Chunk("This report has been automatically created by the JaqpotQuatro report service. Click here to navigate to our official webpage",ffont);
        chunk.setAnchor("http://www.jaqpot.org");

        Paragraph paragraph = new Paragraph(chunk);
        paragraph.add(Chunk.NEWLINE);

        Chapter chapter = new Chapter(paragraph, 1);
        chapter.setNumberDepth(0);

        /** get title */
        String title = null;
        if (report.getMeta()!=null && report.getMeta().getTitles()!=null && !report.getMeta().getTitles().isEmpty())
            title = report.getMeta().getTitles().iterator().next();

        /** print title aligned centered in page */
        if (title ==null) title = "Report";
        chunk = new Chunk(title, chapterFont);
        paragraph = new Paragraph(chunk);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);

        chapter.add(paragraph);

        /** report Description */
        if (report.getMeta()!=null && report.getMeta().getDescriptions()!=null && !report.getMeta().getDescriptions().isEmpty() && report.getMeta().getDescriptions().toString().equalsIgnoreCase("null")) {
            paragraph = new Paragraph();
            paragraph.add(new Chunk("Description: ", paragraphFontBold));
            paragraph.add(new Chunk(report.getMeta().getDescriptions().toString().replaceAll(":http", ": http"), paragraphFont));
            chapter.add(paragraph);
            chapter.add(Chunk.NEWLINE);
        }

        /** report model, algorithm and/or dataset id */
        if (report.getMeta()!=null && report.getMeta().getHasSources()!=null && !report.getMeta().getHasSources().isEmpty() && !report.getMeta().getDescriptions().isEmpty() && report.getMeta().getDescriptions().toString().equalsIgnoreCase("null")) {
            Iterator<String> sources = report.getMeta().getHasSources().iterator();
            sources.forEachRemaining(o -> {
                if (o!=null) {
                    String[] source = o.split("/");
                    if (source[source.length - 2].trim().equals("model") ||
                            source[source.length - 2].trim().equals("algorithm") ||
                            source[source.length - 2].trim().equals("dataset")) {
                        Paragraph paragraph1 = new Paragraph();
                        paragraph1.add(new Chunk(source[source.length - 2].substring(0, 1).toUpperCase() + source[source.length - 2].substring(1) + ": ", paragraphFontBold));
                        paragraph1.add(new Chunk(source[source.length - 1], paragraphFont));
                        chapter.add(paragraph1);
                        chapter.add(Chunk.NEWLINE);
                    }
                }
            });
        }

        /** report single calculations */
        report.getSingleCalculations().forEach((key, value) -> {
            Paragraph paragraph1 = new Paragraph();
            paragraph1 = new Paragraph();
            paragraph1.add(new Chunk(key + ": ", paragraphFontBold));
            paragraph1.add(new Chunk(value.toString().trim().replaceAll(" +", " "), paragraphFont));
            chapter.add(paragraph1);
            chapter.add(Chunk.NEWLINE);
        });

        /** report date of completion */
        if (report.getMeta()!=null && report.getMeta().getDate()!=null) {
            Paragraph paragraph1 = new Paragraph();
            paragraph1.add(new Chunk("Procedure completed on: ", paragraphFontBold));
            paragraph1.add(new Chunk(report.getMeta().getDate().toString(), paragraphFont));
            chapter.add(paragraph1);
            chapter.add(Chunk.NEWLINE);
        }

        try {
            document.add(chapter);
        } catch (DocumentException ex) {
            throw new InternalServerErrorException(ex);
        }

        Integer chapterNumber = 0;

        /** report all_data */
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
                Chapter tableChapter = new Chapter(new Paragraph(tableChunk), ++chapterNumber);
                tableChapter.add(Chunk.NEWLINE);
                tableChapter.add(table);
                document.newPage();
                document.add(tableChapter);
            } catch (DocumentException ex) {
                throw new InternalServerErrorException(ex);
            }
        }

        /** report plots */
        for (Entry<String, String> entry : report.getFigures().entrySet()) {
            try {
                byte[] valueDecoded = Base64.decodeBase64(entry.getValue());
                Image l = Image.getInstance(valueDecoded);
                document.newPage();
                //image starts at the half's half of pdf page
                l.setAbsolutePosition(0, (document.getPageSize().getHeight() / 2 / 2));
                l.scaleToFit(document.getPageSize());

                Chunk tableChunk = new Chunk(entry.getKey(), tableFont);
                Chapter tableChapter = new Chapter(new Paragraph(tableChunk), ++chapterNumber);
                tableChapter.add(l);
                document.add(tableChapter);
            } catch (IOException | DocumentException e) {
                e.printStackTrace();
            }
        }
        document.close();
    }
}
