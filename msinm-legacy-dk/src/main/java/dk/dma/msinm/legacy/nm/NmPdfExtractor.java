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
package dk.dma.msinm.legacy.nm;

import dk.dma.msinm.common.util.TextUtils;
import dk.dma.msinm.model.*;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Massive class for extracting NM messages from PDF files.
 * <p>
 *     The format of the PDF files is that of:
 *     http://www.soefartsstyrelsen.dk/AdvarslerEfterretninger/EfterretningerForSoefarende/Sider/Default.aspx
 * </p>
 */
public class NmPdfExtractor {

    public static final String PDF_NAME_FORMAT = "^(\\d+) EfS (\\d+).pdf$";

    enum Line {

        PREVIOUS(10, "Tidligere EfS.", "Former EfS."),
        REFERENCE(20, "EfS-henvisning.", "EfS reference."),
        TIME(30, "Tid.", "Time."),
        POSITION(40, "Position.", "Position."),
        DETAILS(50, "Detaljer.", "Details."),
        NOTE(60, "Note.", "Note."),
        CHARTS(70, "Søkort.", "Chart(s)."),
        PUBLICATION(80, "Publikation.", "Publication(s)."),
        SOURCE(90, "(", "(");

        int index;
        String nameLocal, nameEnglish;
        private Line(int index, String nameLocal, String nameEnglish) {
            this.index = index;
            this.nameLocal = nameLocal;
            this.nameEnglish = nameEnglish;
        }

        public String getName(String lang) {
            return "da".equals(lang) ? nameLocal : nameEnglish;
        }
    }

    static class LinePart<T> {
        T part;
        String remainingLine;

        public LinePart(T part, String remainingLne) {
            this.part = part;
            this.remainingLine = remainingLne;
        }
    }

    Logger log = LoggerFactory.getLogger(NmPdfExtractor.class);
    String organization;
    InputStream inputStream;
    String fileName;
    int year, week;

    /**
     * Constructor
     *
     * @param file the PDF file
     */
    public NmPdfExtractor(File file, String organization) throws FileNotFoundException {
        this(new FileInputStream(file), file.getName(), organization);
    }

    /**
     * Constructor
     *
     * @param inputStream the PDF input stream
     * @param fileName the name of the PDF file
     */
    public NmPdfExtractor(InputStream inputStream, String fileName, String organization) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.organization = organization;

        Matcher m = getFileNameMatcher(fileName);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid file name, " + fileName + ". Must have format 'yyyy EfS ww.pdf'");
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
     * Main method for extracting the NtM's
     * @param notices the list of notices to update
     */
    public void extractNotices(List<Message> notices) throws Exception {
        PDDocument document = null;
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            document = PDDocument.load(inputStream);
            stripper.setStartPage(3);
            String text = stripper.getText(document);

            List<String> textBlocks = extractNoticeTextBlocks(text);

            extractNotices(notices, textBlocks);

        } catch (IOException e) {
            log.error("Error extracting notivces from file " + fileName, e);
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

    /**
     * Chops the text into blocks of text, each representing an NtM
     * @param text the full text
     * @return the list of NtM texts
     */
    private List<String> extractNoticeTextBlocks(String text) throws IOException {
        List<String> result = new ArrayList<>();

        BufferedReader br = new BufferedReader(new StringReader(text));

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();

            // A new block starts with the message number or "*"
            boolean newBlock = line.matches("^[\\d]+\\..*$");
            if (!newBlock && line.length() > 0 && (int)line.charAt(0) == 61611) {
                newBlock = true;
                line = "*";
            }
            // A translation starts with "Translation"
            boolean translation = line.matches("Translation");

            if (newBlock || translation) {
                StringBuilder block = new StringBuilder();
                block.append(line).append(System.lineSeparator());
                while ((line = br.readLine()) != null) {
                    // Strip header and footer, incl. blank lines
                    if  (!line.matches("^\\s+$") &&
                            !line.startsWith("Efterretninger for Søfarende, uge") &&
                            !line.startsWith("Carl Jacobsens Vej 31")) {
                        block.append(line).append(System.lineSeparator());
                    }

                    // Last line of a block is "(source)"
                    if (line.trim().matches("\\(.+\\)")) {
                        result.add(block.toString());
                        break;
                    }
                }
            }

        }
        return result;
    }

    /**
     * Convert the list of NtM texts into a list of NtM's
     * @param notices the list of NtM's to update
     * @param textBlocks the list of NtM text blocks
     */
    private void extractNotices(List<Message> notices, List<String> textBlocks) throws IOException {

        Message notice = null;
        for (String text : textBlocks) {

            BufferedReader br = new BufferedReader(new StringReader(text));
            String line = br.readLine();

            if (line.matches("Translation")) {
                // English translation of previous Danish notice
                line = br.readLine();
                readNotice(br, line, "en", notice);

            } else {
                // New Danish notice
                notice = new Message();
                notices.add(notice);
                if (line.matches("^\\*")) {
                    notice.setOriginalInformation(true);
                    line = br.readLine();
                }
                // Extract the number
                LinePart<String> parts = readFirstPart(line);
                SeriesIdentifier id = new SeriesIdentifier();
                id.setMainType(SeriesIdType.NM);
                id.setYear(year);
                id.setAuthority(organization);
                id.setNumber(Integer.valueOf(parts.part));
                notice.setSeriesIdentifier(id);
                line = parts.remainingLine;
                readNotice(br, line, "da", notice);

            }
        }
    }

    /**
     * Read the notice from the reader
     * @param br the reader
     * @param line the first line of the notice
     * @param lang the language
     * @param notice the notice to update
     */
    private void readNotice(BufferedReader br, String line, String lang, Message notice) throws IOException {

        MessageDesc desc = notice.checkCreateDesc(lang);

        LinePart<Line> nextLine;
        do {
            nextLine = readLineType(br.readLine(), lang);
            if (nextLine.part != null) {
                break;
            } else {
                line = line + " " + nextLine.remainingLine;
            }
        } while (true);

        // First line -> type
        LinePart<String> parts = readFirstPart(line);
        if ("(P)".equalsIgnoreCase(parts.part)) {
            notice.setType(Type.PRELIMINARY_NOTICE);
            line = parts.remainingLine;
        } else if ("(T)".equalsIgnoreCase(parts.part)) {
            notice.setType(Type.TEMPORARY_NOTICE);
            line = parts.remainingLine;
        } else {
            notice.setType(Type.PERMANENT_NOTICE);
        }

        // First line -> locality and title
        line = removeLastPeriod(line);
        int titleIndex = line.lastIndexOf(".");
        if (titleIndex > 0) {
            readArea(line.substring(0, titleIndex).trim(), lang, notice);
            desc.setTitle(line.substring(titleIndex + 1).trim());
        } else {
            readArea(line, lang, notice);
            desc.setTitle("");
        }

        // Read the notice body
        Line type = nextLine.part;
        line = nextLine.remainingLine;
        do {
            nextLine = readLineType(br.readLine(), lang);
            if (nextLine.part != null) {
                readNoticeField(type, line, lang, notice, desc);
                type = nextLine.part;
                line = nextLine.remainingLine;
                if (type == Line.SOURCE) {
                    readNoticeField(type, line, lang, notice, desc);
                    break;
                }
            } else {
                line = line + "\n" + nextLine.remainingLine;
            }
        } while (true);

    }

    /**
     * Parses the line of a given type and updates the notice accordingly
     * @param type the type of the line
     * @param line the rest of the line
     * @param lang the language
     * @param notice the notice to update
     * @param desc the language-specific notice descriptor
     */
    private void readNoticeField(Line type, String line, String lang, Message notice, MessageDesc desc) {
        switch (type) {
            case CHARTS:
                if ("da".equals(lang)) {
                    readCharts(removeLastPeriod(line), notice);
                }
                break;
            case DETAILS:
                desc.setDescription(TextUtils.txt2html(line));
                break;
            case NOTE:
                desc.setNote(line);
                break;
            case REFERENCE:
                if ("da".equals(lang)) {
                    Arrays.asList(removeLastPeriod(line).split(",|( and )|( og )"))
                            .forEach(ref -> addReference(ref.trim(), ReferenceType.REFERENCE, notice));
                }
                break;
            case POSITION:
                if ("da".equals(lang)) {
                    readLocation(line, lang, notice);
                }
                break;
            case PREVIOUS:
                if ("da".equals(lang)) {
                    if (line.contains("ajourført") || line.contains("ny tid")) {
                        addReference(line, ReferenceType.UPDATE, notice);
                    } else if (line.contains("gentagelse")) {
                        addReference(line, ReferenceType.REPETITION, notice);
                    } else if (line.contains("udgår")) {
                        addReference(line, ReferenceType.CANCELLATION, notice);
                    }
                }
                break;
            case PUBLICATION:
                desc.setPublication(removeLastPeriod(line));
                break;
            case SOURCE:
                desc.setSource(line.substring(0, line.length() - 1));
                break;
            case TIME:
                desc.setTime(line);
                break;
        }

    }

    /**
     * Adds a reference of the given type to the notice.
     * The reference has the format "18/460 2014"
     * @param ref the reference
     * @param type the type
     * @param notice the notice to update
     */
    void addReference(String ref, ReferenceType type, Message notice) {
        Matcher m = Pattern.compile("[-\\d]+/(\\d+) (\\d+).*").matcher(ref);
        if (m.matches()) {
            SeriesIdentifier id = new SeriesIdentifier();
            id.setMainType(SeriesIdType.NM);
            id.setAuthority(organization);
            id.setNumber(Integer.valueOf(m.group(1)));
            id.setYear(Integer.valueOf(m.group(2)));

            Reference reference = new Reference();
            reference.setMessage(notice);
            reference.setType(type);
            reference.setSeriesIdentifier(id);
            notice.getReferences().add(reference);
        }
    }

    /**
     * Reads and updates the area parent hierarchy from the dot-separated list of areas in the line
     * @param line the line containing the areas
     * @param lang the language
     * @param notice the notice to update
     */
    void readArea(String line, String lang, Message notice) {
        String[] areaNames = line.split("\\.");
        Area area = null, parent = null;
        if ("da".equals(lang)) {
            // Create the areas
            for (String name : areaNames) {
                area = new Area();
                area.createDesc(lang).setName(name.trim());
                area.setParent(parent);
                parent = area;
            }
            notice.setArea(area);
        } else {
            // Update the Danish names
            area = notice.getArea();
            for (int x = areaNames.length - 1; x >= 0 && area != null; x--) {
                area.checkCreateDesc("en").setName(areaNames[x].trim());
                area = area.getParent();
            }
        }
    }

    /**
     * Reads the line representing a list of charts and updates the notice accordingly
     * @param line the line
     * @param notice the notice to update
     */
    public void readCharts(String line, Message notice) {
        Pattern p1 = Pattern.compile("(\\d+)");
        Pattern p2 = Pattern.compile("(\\d+) \\(INT (\\d+)\\)");


        for (String chart : line.split(",")) {
            Matcher m1 = p1.matcher(chart.trim());
            Matcher m2 = p2.matcher(chart.trim());
            if (m1.matches()) {
                notice.getCharts().add(new Chart(m1.group(1), null));
            } else if (m2.matches()) {
                notice.getCharts().add(new Chart(m2.group(1), Integer.parseInt(m2.group(2))));
            }
        }
    }


    /**
     * Reads and updates the locations from the line
     * @param locLine the line containing the locations
     * @param lang the language
     * @param notice the notice to update
     */
    void readLocation(String locLine, String lang, Message notice) {
        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        String posPattern = "(\\d+)I?\\s+(\\d+,?\\d+)J?\\s+(N|S)\\s+(\\d+)I?\\s+(\\d+,?\\d+)J?\\s+(E|W),?(.*)";
        Pattern p1 = Pattern.compile(posPattern);
        Pattern p2 = Pattern.compile("(\\d+)\\)\\s+" + posPattern);

        Location location = new Location();
        String[] lines = locLine.split("\n");
        for (int x = 0; x < lines.length; x++) {

            String line = lines[x].trim();
            if (line.endsWith(".")) {
                line = line.substring(0, line.length() - 1);
            }

            Matcher m1 = p1.matcher(line);
            Matcher m2 = p2.matcher(line);
            if (m1.matches() || m2.matches()) {
                Point pt = new Point();
                pt.setLocation(location);
                try {
                    int i = 1;

                    Matcher m;
                    if (m1.matches()) {
                        m = m1;
                        pt.setIndex(x + 1);
                    } else {
                        m = m2;
                        pt.setIndex(Integer.valueOf(m.group(i++)));
                    }

                    pt.setLat(parsePos(
                            Integer.parseInt(m.group(i++)),
                            format.parse(m.group(i++)).doubleValue(),
                            m.group(i++)
                    ));
                    pt.setLon(parsePos(
                            Integer.parseInt(m.group(i++)),
                            format.parse(m.group(i++)).doubleValue(),
                            m.group(i++)
                    ));
                    String desc = m.group(i).trim();
                    if (StringUtils.isNotBlank(desc)) {
                        pt.checkCreateDesc(lang).setDescription(desc);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                location.getPoints().add(pt);
            } else {
                log.warn("No match " + lines[x]);
            }

        }

        if (location.getPoints().size() > 0) {
            location.setType(
                    location.getPoints().size() == 1
                            ? Location.LocationType.POINT
                            : (location.getPoints().size() == 2 ? Location.LocationType.POLYLINE : Location.LocationType.POLYGON));
            notice.getLocations().add(location);
        }
    }

    public static double parsePos(int h, double m, String pos) {
        return h + m / 60.0 * (pos.equalsIgnoreCase("S") || pos.equalsIgnoreCase("W") ? -1 : 1);
    }

    /**
     * Looks at the prefix of the list to determine the type
     * @param line the line to parse
     * @param lang the language
     * @return the tuple of the type and remaining part of the line
     */
    private LinePart<Line> readLineType(String line, String lang) {
        for (Line lineType : Line.values()) {
            String prefix = lineType.getName(lang);
            if (line.trim().startsWith(prefix)) {
                return new LinePart<>(
                        lineType,
                        line.substring(prefix.length()).trim()
                );
            }
        }
        return new LinePart<>(null, line);
    }

    /**
     * Splits the line into the part before and after the first period
     * @param line the line to split
     * @return the two parts of the line before and after the first period
     */
    private LinePart<String> readFirstPart(String line) {
        int i = line.indexOf(".");
        if (i >= 0) {
            return new LinePart<>(
                    line.substring(0, i).trim(),
                    i == line.length() - 1 ? "" : line.substring(i + 1).trim()
            );
        }
        return new LinePart<>(line, "" );
    }

    /**
     * Removes any trailing period from the line
     * @param line the line
     * @return the line excluding any trailing period
     */
    private String removeLastPeriod(String line) {
        line = line.trim();
        if (line.endsWith(".")) {
            line = line.substring(0, line.length() - 1);
        }
        return line;
    }

}
