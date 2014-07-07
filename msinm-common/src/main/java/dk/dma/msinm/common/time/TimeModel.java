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
package dk.dma.msinm.common.time;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.StringWriter;
import java.util.List;

/**
 * The JAX-B implementation of the time-result½ format
 */
@XmlRootElement(name="time-result")
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeModel {

    @XmlElement(name="time")
    private List<Time> times;

    /**
     * Returns an XML representation of this model
     * @return an XML representation of this model
     */
    public String toXml() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(TimeModel.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter out = new StringWriter();
        marshaller.marshal(this, out);
        return out.toString();
    }

    public List<Time> getTimes() {
        return times;
    }

    public void setTimes(List<Time> times) {
        this.times = times;
    }

    /**
     * The JAX-B implementation of the time format
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Time {

        @XmlElement(name = "from")
        DateType fromDate;

        @XmlElement(name = "to")
        DateType toDate;

        @XmlElement(name = "from-to")
        DateType fromToDate;

        public DateType getFromDate() {
            return fromDate;
        }

        public void setFromDate(DateType fromDate) {
            this.fromDate = fromDate;
        }

        public DateType getToDate() {
            return toDate;
        }

        public void setToDate(DateType toDate) {
            this.toDate = toDate;
        }

        public DateType getFromToDate() {
            return fromToDate;
        }

        public void setFromToDate(DateType fromToDate) {
            this.fromToDate = fromToDate;
        }
    }

    /**
     * A tuple entity that contains a date and possibly an hour or hour-range entity
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DateType {

        @XmlElement(name="date")
        Date date;

        @XmlElement(name="hour")
        Hour hour;

        @XmlElement(name="hour-range")
        HourRange hourRange;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Hour getHour() {
            return hour;
        }

        public void setHour(Hour hour) {
            this.hour = hour;
        }

        public HourRange getHourRange() {
            return hourRange;
        }

        public void setHourRange(HourRange hourRange) {
            this.hourRange = hourRange;
        }
    }

    /**
     * Represents a date entity
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Date {

        @XmlAttribute
        Integer day;

        @XmlAttribute
        String month;

        @XmlAttribute
        Integer year;

        @XmlAttribute
        Integer week;

        @XmlAttribute
        String season;

        @XmlAttribute
        Boolean today;

        @XmlAttribute
        Boolean lastdate;

        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getWeek() {
            return week;
        }

        public void setWeek(Integer week) {
            this.week = week;
        }

        public String getSeason() {
            return season;
        }

        public void setSeason(String season) {
            this.season = season;
        }

        public Boolean getToday() {
            return today;
        }

        public void setToday(Boolean today) {
            this.today = today;
        }

        public Boolean getLastdate() {
            return lastdate;
        }

        public void setLastdate(Boolean lastdate) {
            this.lastdate = lastdate;
        }
    }

    /**
     * Represents an hour entity
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Hour {

        @XmlAttribute
        String hour;

        public String getHour() {
            return hour;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }
    }


    /**
     * Represents an hour range entity
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class HourRange {

        @XmlAttribute
        String from;

        @XmlAttribute
        String to;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
