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

/**
 * Common functionality for Time classes
 */
public interface TimeConstants {

    public static final String MONTHS_EN = "January,February,March,April,May,June,July,August,September,October,November,December";

    public static final String SEASONS_EN = "Spring,Summer,Autumn,Winter";

    /**
     * Removes start-end quotes
     * @param value the value to remove quotes from
     * @return the unquoted value
     */
    default String unquote(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static int getMonthIndex(String month) {
        String[] months = MONTHS_EN.split(",");
        for (int x = 0; x < months.length; x++) {
            if (months[x].equalsIgnoreCase(month)) {
                return x;
            }
        }
        return -1;
    }
}
