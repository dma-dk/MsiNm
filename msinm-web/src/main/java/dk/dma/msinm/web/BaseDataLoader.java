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
package dk.dma.msinm.web;

import dk.dma.msinm.common.db.Sql;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.user.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * When the application starts up, this bean will check for the presence of various entities,
 * such as users, area and categories.
 * <p>
 *     If the corresponding tables are empty, the base data SQL will be executed.
 * </p>
 */
@Singleton
@Startup
public class BaseDataLoader extends BaseService {

    @Inject
    private Logger log;

    @Inject
    @Sql("/sql/base-users.sql")
    String usersSql;

    @Inject
    @Sql("/sql/base-categories.sql")
    String categoriesSql;

    @Inject
    @Sql("/sql/base-areas.sql")
    String areasSql;

    @Inject
    @Sql("/sql/base-charts.sql")
    String chartsSql;

    /**
     * Checks whether to load base data or not
     */
    @PostConstruct
    public void init() {

        checkLoadBaseData(User.class, usersSql);
        checkLoadBaseData(Area.class, areasSql);
        checkLoadBaseData(Category.class, categoriesSql);
        checkLoadBaseData(Chart.class, chartsSql);
    }

    /**
     * Performs a check to see if the table of the {@code entityClass} is empty or not.
     * <p>
     *     If it is empty, the {@code importSql} will be executed.
     * </p>
     *
     * @param entityClass the entity class
     * @param importSql the sql to execute, if the table is empty
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void checkLoadBaseData(Class<?> entityClass, String importSql) {

        String table = entityClass.getSimpleName();
        try {
            // Check if the entity table is empty or not
            long count = (Long)em.createQuery("SELECT COUNT(e) FROM " + table + " e").getSingleResult();
            if (count > 0) {
                log.info("Table " + table + " contains " + count +  " rows. Not importing base data.");
            } else {
                log.info("Table " + table + " is empty. Import SQL executed");
                int updateCount = executeScript(importSql);
                log.info("Import SQL executed. " + updateCount + " rows affected");
            }
        } catch (Exception e) {
            log.error("Failed updating table " + table, e);
        }
    }

    /**
     * Executes the SQL script by splitting it into single statements (lines).
     * @param importSql the SQL script to execute
     * @return the number of updated rows
     */
    private int executeScript(String importSql) throws IOException {
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
