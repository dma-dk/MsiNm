package dk.dma.msinm.common.db;

import dk.dma.msinm.common.service.BaseService;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Executes native SQL scripts or statements
 */
@Stateless
public class SqlScriptService extends BaseService {

    /**
     * Executes the SQL script by splitting it into single statements (lines).
     * @param importSql the SQL script to execute
     * @return the number of updated rows
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int executeScript(String importSql) throws IOException {
        int updateCount = 0;
        BufferedReader reader = new BufferedReader(new StringReader(importSql));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (StringUtils.isNotBlank(line) && !line.startsWith("--")) {
                updateCount += em.createNativeQuery(line).executeUpdate();
            }
        }
        return updateCount;
    }

}
