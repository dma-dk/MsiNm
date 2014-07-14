package dk.dma.msinm.common.util;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;

/**
 * Text utility methods
 */
public class TextUtils {

    /**
     * Converts the text from html to plain text
     * @param html the html
     * @return the plain text version
     */
    public static String html2txt(String html) {
        try {
            Document doc = Jsoup.parse(html);
            return new HtmlToPlainText().getPlainText(doc.body());
        } catch (Exception e) {
            // If any error occurs, return the original html
            return html;
        }
    }

    /**
     * Converts the text from plain text to html
     * @param text the text
     * @return the html version
     */
    public static String txt2html(String text) {
        text = StringUtils.replaceEach(text,
                new String[]{"&", "\"", "<", ">", "\n", "\t"},
                new String[]{"&amp;", "&quot;", "&lt;", "&gt;", "<br>", "&nbsp;&nbsp;&nbsp;"});
        return text;
    }

    public static void main(String[] args) {
        System.out.println(txt2html("test\tæøå this\njdhf"));
    }
}
