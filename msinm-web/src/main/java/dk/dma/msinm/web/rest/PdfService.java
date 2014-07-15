/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.web.rest;

import dk.dma.msinm.common.templates.TemplateContext;
import dk.dma.msinm.common.templates.TemplateService;
import dk.dma.msinm.common.templates.TemplateType;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Handles generation of PDF
 */
@Singleton
public class PdfService {

    @Inject
    Logger log;

    @Inject
    TemplateService templateService;


    public void generatePdf(Map<String, Object> data, String template, String language, String bundleName, OutputStream out) throws Exception {

        TemplateContext ctx = templateService.getTemplateContext(TemplateType.PDF, template, data, language, bundleName);
        try {

            String html = templateService.process(ctx);
            Document xhtmlContent = cleanHtml(html);

            String baseUri = "http://localhost:8080";

            long t0 = System.currentTimeMillis();
            log.info("Generating PDF for " + baseUri);

            ITextRenderer renderer = new ITextRenderer();
            // Add font for: font-family: "Arial Unicode MS"
            //renderer.getFontResolver().addFont("/Users/peder/Desktop/xbrt/ARIALUNI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            renderer.setDocument(xhtmlContent, baseUri);
            renderer.layout();

            renderer.createPDF(out);

            log.info("Completed PDF generation in " + (System.currentTimeMillis() - t0) + " ms");


        } catch (Exception e) {
            log.error("error sending email from template " + template, e);
            throw e;
        }

    }

    public Document cleanHtml(String html) {
        Tidy tidy = new Tidy();

        tidy.setShowWarnings(false); //to hide errors
        tidy.setQuiet(true); //to hide warning

        tidy.setXHTML(true);
        return tidy.parseDOM(new StringReader(html), new StringWriter());
    }

}
