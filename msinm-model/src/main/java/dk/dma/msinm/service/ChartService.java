package dk.dma.msinm.service;

import dk.dma.msinm.common.service.BaseService;
import dk.dma.msinm.model.Chart;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

/**
 * Business interface for accessing MSI-NM charts
 */
@Stateless
public class ChartService extends BaseService {

    @Inject
    private Logger log;

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

        return saveEntity(original);
    }

    /**
     * Creates a new chart based on the chart template
     * @param chart the chart to create
     * @return the created chart
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
