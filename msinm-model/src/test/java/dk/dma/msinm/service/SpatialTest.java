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

import dk.dma.msinm.model.Location;
import dk.dma.msinm.model.Point;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static dk.dma.msinm.model.Location.LocationType;

/**
 * Checks the conversion of locations to spatial4j shapes
 * <p>
 * See http://en.wikipedia.org/wiki/Well-known_text
 */
public class SpatialTest {

    @Test
    public void test() throws ParseException {

        List<Point> points = new ArrayList<>();
        points.add(new Point(null, 0.0, 0.0));

        System.out.println("Point " + creteMessageLocation(LocationType.POINT, points).toWkt());

        Location circle = creteMessageLocation(LocationType.CIRCLE, points);
        circle.setRadius(100);
        System.out.println("Circle " + circle.toWkt());

        points.add(new Point(null, 10.0, 0.0));
        points.add(new Point(null, 10.0, 10.0));
        points.add(new Point(null, 0.0, 10.0));
        points.add(new Point(null, 0.0, 0.0));

        System.out.println("Polygon " + creteMessageLocation(LocationType.POLYGON, points).toWkt());
        System.out.println("Polyline " + creteMessageLocation(LocationType.POLYLINE, points).toWkt());
    }


    Location creteMessageLocation(LocationType type, List<Point> points) {
        Location location = new Location();
        location.setType(type);
        location.setPoints(points);
        return location;
    }
}
