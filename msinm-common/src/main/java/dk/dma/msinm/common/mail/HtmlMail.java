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
package dk.dma.msinm.common.mail;

import com.steadystate.css.parser.CSSOMParser;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates a mail from an HTML source.
 * <p></p>
 * The HTML linked sources, such as stylesheets and images, will be inlined.
 * Links will be absolutized (converted from relative links to absolute links).
 * <p></p>
 * There are two ways to inline CSS:
 * <ul>
 *     <li>Inline CSS: The linked stylesheets will be converted to linked inline mail parts</li>
 *     <li>Inline styles: The styles will be parsed and added to each element as a style attribute.
 *     This is quite experimental, but may improve the experience in web-based mail-clients such as gmail.</li>
 * </ul>
 */
public class HtmlMail extends Mail {

    private int cidIndex = 0;
    private Map<String, InlineMailPart> inlinePartsLookup = new HashMap<>();

	/**
	 * Constructor
	 * @param doc the HTML document
     * @param inlineStyles whether to inline CSS styles or not
     * @param includePlainText whether to include a plain text version or not
	 */
	private HtmlMail(Document doc, boolean inlineStyles, boolean includePlainText) throws IOException {
        processHtml(doc, inlineStyles, includePlainText);
	}
	
	/**
	 * Returns a new HTML mail from a URL
	 * 
	 * @param url the URL of the HTML document
     * @param inlineStyles whether to inline CSS styles or not
     * @param includePlainText whether to include a plain text version or not
	 * @return the HTML mail
	 */
	public static HtmlMail fromUrl(String url, boolean inlineStyles, boolean includePlainText) throws IOException {
		Document doc = Jsoup.connect(url).get();
		return new HtmlMail(doc, inlineStyles, includePlainText);
	}
	
	/**
	 * Returns a new HTML mail from the HTML content
	 * 
	 * @param html the actual HTML
	 * @param baseUri the base URI of the document
     * @param inlineStyles whether to inline CSS styles or not
     * @param includePlainText whether to include a plain text version or not
	 * @return the HTML mail
	 */
	public static HtmlMail fromHtml(String html, String baseUri, boolean inlineStyles, boolean includePlainText) throws IOException {
		Document doc = Jsoup.parse(html, baseUri);
		return new HtmlMail(doc, inlineStyles, includePlainText);
	}
	
	/**
	 * Transforms the document to be "mailable" and build up a list of
	 * inline mail parts for images and stylesheets.
	 * 
	 * @param doc the DOM of the HTML document
	 */
	protected void processHtml(Document doc, boolean inlineStyles, boolean includePlainText) throws IOException {
		
		// Clean up a bit
		removeElements(doc, "script", "meta", "base", "iframe");

        if (inlineStyles) {
            // Inline stylesheets
            inlineStyles(doc);

        } else {
            // Inline stylesheets
            inlineStyleSheets(doc);
        }

		// Inline images
		inlineImages(doc);
		
		// Convert links to be absolute
		absolutizeLinks(doc, "a", "href");
		absolutizeLinks(doc, "form", "action");
		
		// Get the result
		setHtmlText(doc.toString());
        setInlineParts(new ArrayList<>(inlinePartsLookup.values()));
        if (includePlainText) {
            setPlainText(new HtmlToPlainText().getPlainText(doc.body()));
        }
	}
	
	/**
	 * Removes the given html elements from the document
	 * 
	 * @param doc the HTML document
	 * @param elements the elements to remove
	 */
	protected void removeElements(Document doc, String... elements) {
		for (Element element : doc.select(Arrays.asList(elements).stream().collect(Collectors.joining(", ")))) {
		    element.remove();
		}		
	}

    /**
     * Inlines the styles of the document.
     * The implementation is somewhat experimental and is inspired by:
     * http://stackoverflow.com/questions/4521557/automatically-convert-style-sheets-to-inline-style
     *
     * @param doc the HTML document
     */
    protected void inlineStyles(Document doc) throws IOException {
        Map<Element, Map<String, String>> allElementsStyles = new HashMap<>();

        // Handle linked style sheets
        for (Element e : doc.select("link")) {

            if ((e.attr("type").equalsIgnoreCase("text/css") || e.attr("rel").equals("stylesheet")) &&
                    !e.attr("media").equalsIgnoreCase("print")) {
                String url = e.absUrl("href");
                if (url.length() > 0) {
                    InputSource source = new InputSource(url);
                    CSSStyleSheet stylesheet = new CSSOMParser().parseStyleSheet(source, null, null);
                    addCssRules(doc, allElementsStyles, stylesheet.getCssRules());
                }
            }

            // Always remove the link
            e.remove();
        }

        // Handle <style> elements
        for (Element e : doc.select("style")) {
            if (!e.attr("media").equalsIgnoreCase("print")) {
                String style = e.html();
                InputSource source = new InputSource(new StringReader(style));
                CSSStyleSheet stylesheet = new CSSOMParser().parseStyleSheet(source, null, null);
                addCssRules(doc, allElementsStyles, stylesheet.getCssRules());
            }

            // Remove the style
            e.remove();
        }


        // Update the elements with the inlined styles
        for (Map.Entry<Element, Map<String, String>> elementEntry : allElementsStyles.entrySet()) {
            Element element = elementEntry.getKey();
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> styleEntry : elementEntry.getValue().entrySet()) {
                builder.append(styleEntry.getKey()).append(":").append(styleEntry.getValue()).append(";");
            }
            builder.append(element.attr("style"));
            element.attr("style", builder.toString());
            // Remove class attributes
            element.removeAttr("class");
        }
    }

    /**
     * Add the css rules from the rule list to the styles map
     * @param doc the HTML document
     * @param allElementsStyles the styles thus far
     * @param ruleList the rules to add
     */
    protected void addCssRules(Document doc, Map<Element, Map<String, String>> allElementsStyles, CSSRuleList ruleList) {
        for (int ruleIndex = 0; ruleIndex < ruleList.getLength(); ruleIndex++) {
            CSSRule item = ruleList.item(ruleIndex);
            if (item instanceof CSSStyleRule) {
                CSSStyleRule styleRule = (CSSStyleRule) item;
                String cssSelector = styleRule.getSelectorText();
                if (cssSelector.contains(":")) {
                    // Skip styles like "a:visited"
                    continue;
                }
                for (Element element : doc.select(cssSelector)) {
                    Map<String, String> elementStyles = allElementsStyles.get(element);
                    if (elementStyles == null) {
                        elementStyles = new LinkedHashMap<>();
                        allElementsStyles.put(element, elementStyles);
                    }
                    CSSStyleDeclaration style = styleRule.getStyle();
                    for (int propertyIndex = 0; propertyIndex < style.getLength(); propertyIndex++) {
                        String propertyName = style.item(propertyIndex);
                        String propertyValue = style.getPropertyValue(propertyName);
                        elementStyles.put(propertyName, propertyValue);
                    }
                }
            }
        }
    }

    /**
	 * Inlines the linked stylesheets, and remove all other link elements
	 * 
	 * @param doc the HTML document
	 */
	protected void inlineStyleSheets(Document doc) {
		Elements elms = doc.select("link");
		for (Element e : elms) {
			
			// Handle style sheets
			if (e.attr("type").equalsIgnoreCase("text/css") || e.attr("rel").equals("stylesheet")) {
				String url = e.absUrl("href");
				if (url.length() == 0) {
					e.remove();
					continue;
				}
				// Change the href to be the inline content id
				InlineMailPart mp = createInlineMailPart(url, "css");
				e.attr("href", "cid:" + mp.getContentId());
				
			} else  {
				// Remove all non-stylesheet links
				e.remove();
			}
		}
	}
	
	/**
	 * Inlines the images
	 * 
	 * @param doc the HTML document
	 */
	protected void inlineImages(Document doc) {
		Elements elms = doc.select("img[src]");
		for (Element e : elms) {
			
			String url = e.absUrl("src");
			if (url.length() == 0) {
				// Weird result...
				e.remove();
				continue;
			}
			// Change the src to be the inline content id
			InlineMailPart mp = createInlineMailPart(url, "img");
			e.attr("src", "cid:" + mp.getContentId());
		}
	}
	
	/**
	 * Make sure that links are absolute. 
	 * Used for a.href and form.action links
	 * 
	 * @param doc the HTML document
	 */
	protected void absolutizeLinks(Document doc, String tag, String attr) {
		Elements elms = doc.select(tag + "[" + attr + "]");
		for (Element e : elms) {
			
			String url = e.absUrl(attr);
			if (url.length() == 0) {
				// Disable link
				e.attr(attr, "#");
				continue;
			}
			// Update the link to be the absolute link
			e.attr(attr, url);
		}
	}
	
	/**
	 * Create or look up an existing inline mail part for the given url.
	 * Hmm, we make the assumption that e.g. a stylesheet and an image 
	 * cannot have the same url...
	 * 
	 * @param url the url of the resource
	 * @param name the cid name prefix
	 * @return the associated inline mail part
	 */
	protected InlineMailPart createInlineMailPart(String url, String name) {
		// Check if this is a known url
		InlineMailPart mp = inlinePartsLookup.get(url);
		if (mp == null) {
			String cid = name + (cidIndex++);
			mp = new InlineMailPart(cid, url);
            inlinePartsLookup.put(url, mp);
		}
		return mp;
	}
}
