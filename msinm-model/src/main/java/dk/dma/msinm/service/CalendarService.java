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

import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.common.settings.annotation.Setting;
import dk.dma.msinm.common.util.TextUtils;
import dk.dma.msinm.vo.AreaVo;
import dk.dma.msinm.vo.MessageVo;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Support for generating iCalendar data from message data, according to the RFC2445 specification.
 * <p>
 *     iCal4j is used as the Java API for generating calendar data.
 *     Please refer to http://wiki.modularity.net.au/ical4j/
 *     and examples at: http://wiki.modularity.net.au/ical4j/index.php?title=Examples#Creating_an_event
 * </p>
 * <p>
 *     For now, the validFrom and validTo dates are used.
 *     Later on, parse the message time field for a more precise date interval
 * </p>
 */
public class CalendarService {

    @Inject
    private Logger log;

    @Inject
    @Setting(value = "reportRecipient", defaultValue = "msinm@e-navigation.net")
    String reportRecipient;

    /**
     * Generates iCalendar data from the list of messages in the given language
     *
     * @param messages the list of messages
     * @param lang     the language
     * @param out  the output stream
     */
    public void generateCalendarData(List<MessageVo> messages, String lang, OutputStream out) throws IOException, ValidationException {

        Calendar ical = new Calendar();
        PropertyList props = ical.getProperties();
        props.add(new ProdId("-//MSI NM Calendar//DMA 1.0//" + lang.toUpperCase()));
        props.add(Version.VERSION_2_0);
        props.add(CalScale.GREGORIAN);

        // Instantiate UID generator.
        UidGenerator ug = new UidGenerator("uidGen");

        // Add all the messages
        messages.forEach(msg -> {
            VEvent evt = new VEvent(new Date(msg.getValidFrom()), composeTitle(msg, lang));
            PropertyList evtProps = evt.getProperties();
            evtProps.add(ug.generateUid());

            // Set the end date
            if (msg.getValidTo() != null) {
                evtProps.add(new DtEnd(new Date(msg.getValidTo())));
            }

            // Make it all-day event for now.
            //evtProps.getProperty(Property.DTSTART).getParameters().add(Value.DATE);

            // Add contact recipient
            if (StringUtils.isNotBlank(reportRecipient)) {
                try {
                    evtProps.add(new Organizer(new URI("mailto:" + reportRecipient)));
                } catch (URISyntaxException e) {
                    log.debug("Error formatting email for calendar event " + reportRecipient);
                }
            }

            // Add description
            String description = composeDescription(msg, lang);
            if (StringUtils.isNotBlank(description)) {
                evtProps.add(new Description(description));
            }

            ical.getComponents().add(evt);
        });

        log.info("Created calendar with " + ical.getComponents().size() + " events");
        new CalendarOutputter().output(ical, out);
    }

    /**
     * Composes a title for the calendar event
     *
     * @param msg  the message
     * @param lang the language
     * @return the title to use for the calendar event
     */
    private String composeTitle(MessageVo msg, String lang) {
        StringBuilder title = new StringBuilder();

        title.append(msg.getSeriesIdentifier().getFullId());

        try {
            title.append(" ")
                    .append(msg.getDescs(DataFilter.lang(lang)).get(0).getTitle());
        } catch (Exception e) {
            log.debug("No message description for message " + msg.getId());
        }
        return title.toString();
    }

    /**
     * Composes a description for the calendar event
     * @param msg the message
     * @param lang the language
     * @return the description to use for the calendar event
     */
    private String composeDescription(MessageVo msg, String lang) {

        StringBuilder description = new StringBuilder();

        String areaDesc = null;
        for (AreaVo area = msg.getArea(); area != null; area = area.getParent()) {
            areaDesc = area.getDescs(DataFilter.lang(lang)).get(0).getName()
                    + (areaDesc == null ? "" : " - " + areaDesc);
        }

        description.append("Area: " + areaDesc);

        MessageVo.MessageDescVo desc = msg.getDescs(DataFilter.lang(lang)).get(0);
        if (StringUtils.isNotBlank(desc.getVicinity())) {
            description.append(" - ").append(desc.getVicinity());
        }
        if (StringUtils.isNotBlank(desc.getTitle())) {
            description.append(" - ").append(desc.getTitle());
        }
        description.append("\n");

        if (StringUtils.isNotBlank(desc.getDescription())) {
            description.append(TextUtils.html2txt(desc.getDescription()));
        }

        return description.toString();
    }
}
