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
import dk.dma.msinm.common.db.SqlScriptService;
import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Area;
import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.service.AreaService;
import dk.dma.msinm.service.CategoryService;
import dk.dma.msinm.templates.model.ListParamType;
import dk.dma.msinm.templates.service.TemplateService;
import dk.dma.msinm.user.User;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

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
    AreaService areaService;

    @Inject
    CategoryService categoryService;

    @Inject
    TemplateService templateService;

    @Inject
    SqlScriptService sqlScriptService;

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

    @Inject
    @Sql("/sql/base-template-params.sql")
    String baseTemplateParamsSql;

    /**
     * Checks whether to load base data or not
     */
    @PostConstruct
    public void init() {

        checkLoadBaseData(User.class, usersSql);

        checkLoadBaseData(Chart.class, chartsSql);

        if (checkLoadBaseData(Area.class, areasSql)) {
            areaService.updateLineages();
        }

        if (checkLoadBaseData(Category.class, categoriesSql)) {
            categoryService.updateLineages();
        }

        if (checkLoadBaseData(ListParamType.class, baseTemplateParamsSql)) {
            templateService.loadBaseTemplateData("/sql/base-templates.json");
        }
    }

    /**
     * Performs a check to see if the table of the {@code entityClass} is empty or not.
     * <p>
     *     If it is empty, the {@code importSql} will be executed.
     * </p>
     *
     * @param entityClass the entity class
     * @param importSql the sql to execute, if the table is empty
     * @return if data was imported
     */
    private boolean checkLoadBaseData(Class<?> entityClass, String importSql) {

        String table = entityClass.getSimpleName();
        try {
            // Check if the entity table is empty or not
            long count = (Long)em.createQuery("SELECT COUNT(e) FROM " + table + " e").getSingleResult();
            if (count > 0) {
                log.info("Table " + table + " contains " + count +  " rows. Not importing base data.");
            } else {
                log.info("Table " + table + " is empty. Import SQL executed");
                int updateCount = sqlScriptService.executeScript(importSql);
                log.info("Import SQL executed. " + updateCount + " rows affected");
                return true;
            }
        } catch (Exception e) {
            log.error("Failed updating table " + table, e);
        }
        return false;
    }
}
