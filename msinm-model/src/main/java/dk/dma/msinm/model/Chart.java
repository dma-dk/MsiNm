package dk.dma.msinm.model;

import dk.dma.msinm.common.model.VersionedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

/**
 * Represents a chart
 */
@Entity
@NamedQueries({
        @NamedQuery(name="Chart.findByChartNumber",
                query="SELECT chart FROM Chart chart where chart.chartNumber = :chartNumber")
})
public class Chart extends VersionedEntity<Integer> {

    @NotNull
    @Column(unique = true)
    String chartNumber;

    Integer internationalNumber;

    String horizontalDatum;

    public Chart() {
    }

    public Chart(String chartNumber, Integer internationalNumber) {
        this.chartNumber = chartNumber;
        this.internationalNumber = internationalNumber;
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
