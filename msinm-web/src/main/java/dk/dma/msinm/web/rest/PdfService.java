package dk.dma.msinm.web.rest;

import freemarker.template.Configuration;
import freemarker.template.Template;
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
    Configuration mailTemplateConfiguration; // TODO PDF template


    public void generatePdf(Map<String, Object> data, String template, OutputStream out) throws Exception {
        Template fmTemplate;
        try {
            fmTemplate = mailTemplateConfiguration.getTemplate(template);

            StringWriter html = new StringWriter();
            fmTemplate.process(data, html);

            Document xhtmlContent = cleanHtml(html.toString());

            String baseUri = "http://localhost:8080";
            data.put("baseUri", baseUri);

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
