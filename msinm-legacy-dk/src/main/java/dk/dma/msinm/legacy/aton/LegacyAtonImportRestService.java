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
package dk.dma.msinm.legacy.aton;

import dk.dma.msinm.common.repo.RepositoryService;
import dk.dma.msinm.model.AtoN;
import dk.dma.msinm.service.AtoNService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.*;

/**
 * Imports AtoN from Excel sheets.
 */
@Path("/import/legacy-aton-import")
@Stateless
public class LegacyAtonImportRestService {

    @Inject
    Logger log;

    @Context
    ServletContext servletContext;

    @Inject
    AtoNService atonService;

    /**
     * Imports an uploaded AtoN Excel file
     *
     * @param request the servlet request
     * @return a status
     */
    @POST
    @Path("/upload-xls")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    public String importXls(@Context HttpServletRequest request) throws Exception {
        FileItemFactory factory = RepositoryService.newDiskFileItemFactory(servletContext);
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        for (FileItem item : items) {
            if (!item.isFormField()) {
                // AtoN
                if (item.getName().toLowerCase().endsWith(".xls")) {
                    StringBuilder txt = new StringBuilder();
                    importAtoN(item.getInputStream(), item.getName(), txt);
                    return txt.toString();
                }
            }
        }

        return "No valid PDF uploaded";
    }

    /**
     * Extracts the AtoNs from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importAtoN(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting AtoNs from Excel sheet " + fileName);

        List<AtoN> atons = new ArrayList<>();

        // Create Workbook instance holding reference to .xls file
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        // Get first/desired sheet from the workbook
        HSSFSheet sheet = workbook.getSheetAt(0);

        int x = 0;

        // Get row iterator
        Iterator<Row> rowIterator = sheet.iterator();
        Row headerRow = rowIterator.next();

        // Get the column indexes of the relevant columns
        Map<String, Integer> colIndex = new HashMap<>();
        updateColumnIndex(headerRow, colIndex, "AFMSTATION");
        updateColumnIndex(headerRow, colIndex, "AFM_NAVN");
        updateColumnIndex(headerRow, colIndex, "AFUFORKORTELSE");
        updateColumnIndex(headerRow, colIndex, "BESKRIVELSE");
        updateColumnIndex(headerRow, colIndex, "LATTITUDE");
        updateColumnIndex(headerRow, colIndex, "LONGITUDE");
        updateColumnIndex(headerRow, colIndex, "KARAKNR");
        updateColumnIndex(headerRow, colIndex, "EJER");

        // Extract the AtoNs
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            AtoN aton = new AtoN();

            aton.setAtonUid(row.getCell(colIndex.get("AFMSTATION")).getStringCellValue());
            aton.setName(row.getCell(colIndex.get("AFM_NAVN")).getStringCellValue());
            aton.setCode(row.getCell(colIndex.get("AFUFORKORTELSE")).getStringCellValue());
            aton.setDescription(row.getCell(colIndex.get("BESKRIVELSE")).getStringCellValue());
            aton.setOwner(row.getCell(colIndex.get("EJER")).getStringCellValue());
            aton.setLat(row.getCell(colIndex.get("LATTITUDE")).getNumericCellValue());
            aton.setLon(row.getCell(colIndex.get("LONGITUDE")).getNumericCellValue());


            // In this simplified "type" mapping, we map the "karaknr" field to the value 1-3.
            // "karaknr" values:
            // 0: AIS
            // 1: Fyr
            // 2: Bifyr, tågelys, advarselslys, retningsfyr, hindringslys, m.v.
            // 3: Båker, Signalmaster
            // 4: RACONS
            // 5: Lystønder
            // 6: Vagere
            // 7: Stager i bund
            // 8: Radiofyr
            // 9: Tågesignaler

            String karaknr = String.valueOf(Math.round(row.getCell(colIndex.get("KARAKNR")).getNumericCellValue()));
            if (karaknr.contains("5")) {
                aton.setType(2);
            } else if (karaknr.contains("6") || karaknr.contains("7")) {
                aton.setType(3);
            } else {
                aton.setType(1);
            }

            atons.add(aton);
        }

        // Update the AtoN database
        atonService.replaceAtoNs(atons);

        log.info("Extracted " + atons.size() + " AtoNs from " + fileName);
        txt.append("Extracted " + atons.size() + " AtoNs from " + fileName + "\n");
    }

    /** Determines the column index of the given column name */
    private boolean updateColumnIndex(Row headerRow, Map<String, Integer> colIndex, String colName) {
        int index = 0;
        for (Cell cell : headerRow) {
            if (cell.getCellType() == Cell.CELL_TYPE_STRING &&
                    colName.equalsIgnoreCase(cell.getStringCellValue())) {
                colIndex.put(colName, index);
                return true;
            }
            index++;
        }
        return false;
    }

}
