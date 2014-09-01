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
package dk.dma.msinm.service;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Chart;
import dk.dma.msinm.vo.ChartVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Business interface for accessing MSI-NM charts
 */
@Stateless
public class ChartService extends BaseService {

    @Inject
    private Logger log;

    /**
     * Searchs for charts matching the given term
     * @param term the search term
     * @param limit the maximum number of results
     * @return the search result
     */
    public List<ChartVo> searchCharts(String term, int limit) {
        List<ChartVo> result = new ArrayList<>();
        if (StringUtils.isNotBlank(term)) {
            List<Chart> charts = em
                    .createNamedQuery("Chart.searchCharts", Chart.class)
                    .setParameter("term", "%" + term + "%")
                    .setParameter("sort", term)
                    .setMaxResults(limit)
                    .getResultList();

           charts.forEach(chart -> result.add(new ChartVo(chart)));
        }
        return result;
    }

    /**
     * Returns the list of charts
     * @return the list of charts
     */
    public List<Chart> getCharts() {
        return getAll(Chart.class);
    }

    /**
     * Returns the chart with the given chart number
     * @param chartNumber the chart number
     * @return the chart with the given chart number
     */
    public Chart findByChartNumber(String chartNumber) {
        try {
            return em
                    .createNamedQuery("Chart.findByChartNumber", Chart.class)
                    .setParameter("chartNumber", chartNumber)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Updates the chart data from the chart template
     * @param chart the chart to update
     * @return the updated chart
     */
    public Chart updateChartData(Chart chart) {
        Chart original = getByPrimaryKey(Chart.class, chart.getId());

        // Copy the chart data
        original.setChartNumber(chart.getChartNumber());
        original.setInternationalNumber(chart.getInternationalNumber());
        original.setHorizontalDatum(chart.getHorizontalDatum());
        original.setName(chart.getName());
        original.setScale(chart.getScale());

        return saveEntity(original);
    }

    /**
     * Creates a new chart based on the chart template
     * @param chart the chart to create
     * @return the created chart
     */
    public Chart createChart(Chart chart) {

        return saveEntity(chart);
    }

    /**
     * Deletes the chart
     * @param chartId the id of the chart to delete
     */
    public boolean deleteChart(Integer chartId) {

        Chart chart = getByPrimaryKey(Chart.class, chartId);
        if (chart != null) {
            remove(chart);
            return true;
        }
        return false;
    }


}
