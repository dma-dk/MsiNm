package dk.dma.msinm.vo;

import dk.dma.msinm.common.vo.BaseVo;
import dk.dma.msinm.model.Chart;
import org.apache.commons.lang.StringUtils;

/**
 * Value object for charts
 */
public class ChartVo extends BaseVo<Chart> {

    Integer id;
    String chartNumber;
    Integer internationalNumber;
    String horizontalDatum;

    /**
     * Constructor
     */
    public ChartVo() {
    }

    /**
     * Constructor
     */
    public ChartVo(Chart entity) {
        super(entity);
        id = entity.getId();
        chartNumber = entity.getChartNumber();
        internationalNumber = entity.getInternationalNumber();
        horizontalDatum = entity.getHorizontalDatum();
    }

    @Override
    public Chart toEntity() {
        Chart chart = new Chart();
        chart.setId(id);
        chart.setChartNumber(StringUtils.trim(chartNumber));
        chart.setInternationalNumber(internationalNumber);
        chart.setHorizontalDatum(StringUtils.trim(horizontalDatum));
        return chart;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChartNumber() {
        return chartNumber;
    }

    public void setChartNumber(String chartNumber) {
        this.chartNumber = chartNumber;
    }

    public Integer getInternationalNumber() {
        return internationalNumber;
    }

    public void setInternationalNumber(Integer internationalNumber) {
        this.internationalNumber = internationalNumber;
    }

    public String getHorizontalDatum() {
        return horizontalDatum;
    }

    public void setHorizontalDatum(String horizontalDatum) {
        this.horizontalDatum = horizontalDatum;
    }
}
