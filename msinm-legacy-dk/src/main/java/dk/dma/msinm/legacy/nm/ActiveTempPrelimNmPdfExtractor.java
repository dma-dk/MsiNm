package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.model.SeriesIdentifier;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for extracting active list of P&T NM messages from PDF files.
 * <p>
 *     The format of the PDF files is that of:
 *     http://www.soefartsstyrelsen.dk/AdvarslerEfterretninger/EfterretningerForSoefarende/Sider/Default.aspx
 * </p>
 */
public class ActiveTempPrelimNmPdfExtractor {

    public static final String PDF_NAME_FORMAT = "^(\\d+) PogT (\\d+).pdf$";
    public static final String ACTIVE_NM_LINE = "^[-\\d]+/(\\d+) \\([T|P]\\) .*";

    Logger log = LoggerFactory.getLogger(NmPdfExtractor.class);
    InputStream inputStream;
    String fileName;
    int year, week;

    /**
     * Constructor
     *
     * @param file the PDF file
     */
    public ActiveTempPrelimNmPdfExtractor(File file) throws FileNotFoundException {
        this(new FileInputStream(file), file.getName());
    }

    /**
     * Constructor
     *
     * @param inputStream the PDF input stream
     * @param fileName the name of the PDF file
     */
    public ActiveTempPrelimNmPdfExtractor(InputStream inputStream, String fileName) {
        this.inputStream = inputStream;
        this.fileName = fileName;

        Matcher m = getFileNameMatcher(fileName);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid file name, " + fileName + ". Must have format 'yyyy PogT ww.pdf'");
        }
        this.year = Integer.valueOf(m.group(1));
        this.week = Integer.valueOf(m.group(2));
    }

    /**
     * Returns a matcher for the file name
     * @param fileName the file name
     * @return the matcher
     */
    public static Matcher getFileNameMatcher(String fileName) {
        Pattern p = Pattern.compile(PDF_NAME_FORMAT);
        return p.matcher(fileName);
    }

    public int getYear() {
        return year;
    }

    public int getWeek() {
        return week;
    }

    /**
     * Main method for extracting active list of NtM's
     * @param noticeIds the list of notices to update
     */
    public void extractActiveNoticeIds(List<SeriesIdentifier> noticeIds) throws Exception {
        PDDocument document = null;
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            document = PDDocument.load(inputStream);
            //stripper.setStartPage(1);
            String text = stripper.getText(document);

            // Read the text line by line
            Pattern p = Pattern.compile(ACTIVE_NM_LINE);
            BufferedReader br = new BufferedReader(new StringReader(text));
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line.trim());
                if (m.matches()) {
                    SeriesIdentifier id = new SeriesIdentifier();
                    id.setYear(year);
                    id.setNumber(Integer.valueOf(m.group(1)));
                    id.setAuthority("DMA");
                    noticeIds.add(id);
                }
            }

        } catch (IOException e) {
            log.error("Error extracting notices from file " + fileName, e);
            throw e;
        } finally {
            if (document != null) {
                document.close();
            }
            try {
                inputStream.close();
            } catch (Exception ex) {
            }
        }
    }
}
