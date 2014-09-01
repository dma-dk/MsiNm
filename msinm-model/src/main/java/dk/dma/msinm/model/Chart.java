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
        @NamedQuery(name  = "Chart.searchCharts",
                query = "select distinct c from Chart c where lower(c.chartNumber) like lower(:term) "
                        + "or ('' + c.internationalNumber) like lower(:term) "
                        + "or lower(c.name) like lower(:term) "
                        + "order by "
                        + "case when LOCATE(lower(:sort), lower(c.chartNumber)) = 0 and LOCATE(lower(:sort), lower(c.name)) = 0 then LOCATE(lower(:sort), '' + c.internationalNumber) "
                        + "when LOCATE(lower(:sort), lower(c.chartNumber)) = 0 then LOCATE(lower(:sort), c.name) "
                        + "else LOCATE(lower(:sort), lower(c.chartNumber)) end, chartNumber"),
        @NamedQuery(name="Chart.findByChartNumber",
                query="SELECT chart FROM Chart chart where chart.chartNumber = :chartNumber")
})
public class Chart extends VersionedEntity<Integer> {

    @NotNull
    @Column(unique = true)
    String chartNumber;

    Integer internationalNumber;

    String horizontalDatum;

    Integer scale;

    String name;

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

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
