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
package dk.dma.msinm.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.Version;

/**
 * Lucene utility methods
 */
public class LuceneUtils {

    public final static Version LUCENE_VERSION = Version.LUCENE_47;

    final static String ACCENTED_CHARS = "ÁÀÄÂáàäâÉÈËÊéèëêÍÌÏÎíìïîÓÒÖÔóòöôÚÙÜÛúùüûÝýÑñ";
    final static String REPLACED_CHARS = "AAAAaaaaEEEEeeeeIIIIiiiiOOOOooooUUUUuuuuYyNn";

    /**
     * No-access constructor
     */
    private LuceneUtils() {
    }

    /**
     * Normalizes the string by replacing all accented chars
     * with non-accented versions
     *
     * @param txt the text to update
     * @return the normalized text
     */
    public static String normalize(String txt) {
        return StringUtils.replaceChars(
                txt,
                ACCENTED_CHARS,
                REPLACED_CHARS);
    }

    /**
     * Normalizes the string by replacing all accented chars
     * with non-accented versions and ensure that lowercase
     * "and" and "or" operators are supported
     *
     * @param txt the text to update
     * @return the normalized text
     */
    public static String normalizeQuery(String txt) {
        if (txt == null) {
            return null;
        }

        return normalize(txt)
                .replaceAll(" or ", " OR ")
                .replaceAll(" and ", " AND ");
    }
}
