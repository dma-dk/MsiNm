package dk.dma.msinm.model;

import dk.dma.msinm.common.model.VersionedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Represents a chart
 */
@Entity
public class Chart extends VersionedEntity<Integer> {

    @NotNull
    @Column(unique = true)
    Integer chartNumber;

    Integer internationalNumber;

    String horizontalDatum;

    public Integer getChartNumber() {
        return chartNumber;
    }

    public void setChartNumber(Integer chartNumber) {
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
