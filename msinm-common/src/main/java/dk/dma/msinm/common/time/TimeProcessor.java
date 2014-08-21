package dk.dma.msinm.common.time;

import javax.xml.bind.JAXBException;
import java.util.Calendar;

/**
 * Processes the TimeModel structure
 */
public class TimeProcessor implements TimeConstants {

    /**
     * Processes the time model by performing various transformations.
     * Afterwards, each all from-to dates will have been converted to
     * a from and a to-date, and all date fields such as "lastday",
     * "today", "week" and "season", will have been computed.
     * <p>
     *     Please note, the actual model passed in will be modified.
     * </p>
     * @param model the time model
     * @return the processed model
     */
    public static TimeModel process(TimeModel model) {

        // Handle special case where first time records designates a date
        // and remaining records refers to the "lastdate" first record
        if (model.getTimes().size() > 1 && model.getTimes().get(0).getFromToDate() != null) {
            TimeModel.DateType dateType = model.getTimes().get(1).toDateList().stream()
                    .filter(d -> d.date != null && d.date.lastdate != null && d.date.lastdate)
                    .findFirst()
                    .get();
            if (dateType != null) {
                TimeModel.Date date = model.getTimes().remove(0).getFromToDate().date;
                model.getTimes().forEach(t -> t.toDateList().stream()
                    .filter(dt -> dt.date != null && dt.date.lastdate != null && dt.date.lastdate)
                    .map(dt -> dt.date)
                    .forEach(d -> {
                        d.lastdate = null;
                        if (d.year == null) {
                            d.year = date.year;
                        }
                        if (d.month == null) {
                            d.month = date.month;
                        }
                    })
                );
            }
        }

        // Process the "today" field
        model.getTimes().forEach(t -> t.toDateList().stream()
            .filter(d -> d.date != null && d.date.today != null && d.date.today)
            .forEach(d -> {
                Calendar today = Calendar.getInstance();
                d.date.setToday(null);
                d.date.setYear(today.get(Calendar.YEAR));
                d.date.setMonth(MONTHS_EN.split(",")[today.get(Calendar.MONTH)].toLowerCase());
                d.date.setDay(today.get(Calendar.DAY_OF_MONTH));
            }));

        // Replace from-to fields with from and to dates
        model.getTimes().stream()
                .filter(t -> t.getFromToDate() != null)
                .forEach(t -> {
                    t.fromDate = new TimeModel.DateType(t.fromToDate);
                    t.toDate = new TimeModel.DateType(t.fromToDate);
                    t.fromToDate = null;
                });

        // Process "week" and "month" fields
        model.getTimes().stream()
                .filter(t -> t.fromDate != null && t.fromDate.date != null)
                .map(t -> t.fromDate.date)
                .forEach(d -> d.processDate(true));
        model.getTimes().stream()
                .filter(t -> t.toDate != null && t.toDate.date != null)
                .map(t -> t.toDate.date)
                .forEach(d -> d.processDate(false));

        return  model;
    }

    /**
     * Computes the start and end dates of the time model
     * @param model the time model
     * @return the start and end dates. May be null.
     */
    public static java.util.Date[] getDateInterval(TimeModel model) {
        final java.util.Date[] dates = new java.util.Date[3];

        model = process(model);

        model.getTimes().stream()
                .filter(t -> t.fromDate != null && t.fromDate.date != null)
                .map(t -> t.fromDate.date)
                .forEach(d -> {
                    Calendar cal = d.toDate();
                    if (dates[0] == null || cal.getTime().before(dates[0])) {
                        dates[0] = cal.getTime();
                    }
                });

        model.getTimes().stream()
                .filter(t -> t.toDate != null && t.toDate.date != null)
                .map(t -> t.toDate.date)
                .forEach(d -> {
                    Calendar cal = d.toDate();
                    if (dates[1] == null || cal.getTime().after(dates[1])) {
                        dates[1] = cal.getTime();
                    }
                });

        return dates;
    }

    public static void main(String... args) throws TimeException, JAXBException {
        TimeParser parser = TimeParser.get();

        //TimeModel model = parser.parseModel("Mid-July - end October 2014.");
        //TimeModel model = parser.parseModel("Until October 2014.");
        TimeModel model = parser.parseModel("June 2014.\n10 hours 0900 - 1500.\n11 - 12 hours 0910 - 1345.");

        model = process(model);

        System.out.println(model.toXml());

    }
}
