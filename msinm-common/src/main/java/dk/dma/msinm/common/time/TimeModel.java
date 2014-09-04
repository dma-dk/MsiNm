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
import java.util.ArrayList;
import java.util.Calendar;
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

        /**
         * Returns the date fields of this entity
         * @return the date fields of this entity
         */
        public List<DateType> toDateList() {
            List<DateType> result = new ArrayList<>();
            if (fromDate != null) {
                result.add(fromDate);
            }
            if (toDate != null) {
                result.add(toDate);
            }
            if (fromToDate != null) {
                result.add(fromToDate);
            }
            return result;
        }

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

        public DateType() {
        }

        public DateType(DateType other) {
            this.date = (other.date == null) ? null : new Date(other.date);
            this.hour = other.hour;
            this.hourRange = other.hourRange;
        }

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

        public Date() {
        }

        public Date(Date other) {
            this.day = other.day;
            this.month = other.month;
            this.year = other.year;
            this.week = other.week;
            this.season = other.season;
            this.today = other.today;
            this.lastdate = other.lastdate;
        }

        /**
         * Processes this date entity by resolving attributes such as week and season.
         * @param periodStart whether this entity is the start or end of a week or season entity
         */
        public void processDate(boolean periodStart) {
            Calendar cal = Calendar.getInstance();
            if (year == null) {
                year = cal.get(Calendar.YEAR);
            }
            if (week != null) {
                cal.set(Calendar.WEEK_OF_YEAR, week);
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.set(Calendar.DAY_OF_WEEK, (periodStart) ? Calendar.MONDAY : Calendar.SUNDAY);
                week = null;
                month = TimeConstants.MONTHS_EN.split(",")[cal.get(Calendar.MONTH)].toLowerCase();
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
            if (season != null) {
                if ("Spring".equalsIgnoreCase(season)) {
                    cal.set(Calendar.MONTH, periodStart ? 2 : 4);
                } else if ("Summer".equalsIgnoreCase(season)) {
                    cal.set(Calendar.MONTH, periodStart ? 5 : 7);
                } else if ("Autumn".equalsIgnoreCase(season)) {
                    cal.set(Calendar.MONTH, periodStart ? 8 : 10);
                } else if ("Winter".equalsIgnoreCase(season)) {
                    cal.set(Calendar.MONTH, periodStart ? 11 : 1);
                }
                cal.set(Calendar.DAY_OF_MONTH, periodStart ? 1 : cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                month = TimeConstants.MONTHS_EN.split(",")[cal.get(Calendar.MONTH)].toLowerCase();
                day = cal.get(Calendar.DAY_OF_MONTH);
                season = null;
            }
            if (month != null) {
                cal.set(Calendar.MONTH, TimeConstants.getMonthIndex(month));
            }
            if (day != null && day == 99) {
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
            if (day == null) {
                cal.set(Calendar.DAY_OF_MONTH, periodStart ? 1 : cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
        }

        /**
         * Converts this entity to a date. Before calling this method, the
         * TimeModel should have been processed with the TimeProcessor
         * @return the date
         */
        public Calendar toDate() {
            Calendar cal = Calendar.getInstance();
            if (year != null) {
                cal.set(Calendar.YEAR, year);
            }
            if (month != null) {
                cal.set(Calendar.MONTH, TimeConstants.getMonthIndex(month));
            }
            if (day != null) {
                cal.set(Calendar.DAY_OF_MONTH, day);
            }
            return cal;
        }

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
