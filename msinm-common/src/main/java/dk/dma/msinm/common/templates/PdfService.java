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
package dk.dma.msinm.common.templates;

import dk.dma.msinm.common.MsiNmApp;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.inject.Inject;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Handles generating PDF files based on a FreeMarker template
 */
public class PdfService {

    @Inject
    Logger log;

    @Inject
    TemplateService templateService;

    @Inject
    MsiNmApp app;

    /**
     * Generates a PDF based on the Freemarker template and streams it to the output stream
     *
     * @param data the data to use in the Freemarker template
     * @param template the Freemarker template
     * @param language the language
     * @param bundleName the resource bundle name to load
     * @param out the output stream
     */
    public void generatePdf(Map<String, Object> data, String template, String language, String bundleName, OutputStream out) throws Exception {

        // Looks up the PDF template
        TemplateContext ctx = templateService.getTemplateContext(TemplateType.PDF, template, data, language, bundleName);
        try {

            // Process the template and clean up the resulting html
            String html = templateService.process(ctx);
            Document xhtmlContent = cleanHtml(html);

            String baseUri = app.getBaseUri();
            long t0 = System.currentTimeMillis();
            log.info("Generating PDF for " + baseUri);

            // Generate PDF from the HTML
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(xhtmlContent, baseUri);
            renderer.layout();
            renderer.createPDF(out);

            log.info("Completed PDF generation in " + (System.currentTimeMillis() - t0) + " ms");


        } catch (Exception e) {
            log.error("error sending email from template " + template, e);
            throw e;
        }

    }

    /**
     * Use JTidy to clean up the HTML
     * @param html the HTML to clean up
     * @return the resulting XHTML
     */
    public Document cleanHtml(String html) {
        Tidy tidy = new Tidy();

        tidy.setShowWarnings(false); //to hide errors
        tidy.setQuiet(true); //to hide warning

        tidy.setXHTML(true);
        return tidy.parseDOM(new StringReader(html), new StringWriter());
    }

}
