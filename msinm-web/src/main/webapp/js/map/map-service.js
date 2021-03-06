
/**
 * MapService factory
 */
angular.module('msinm.map')
       .factory('MapService', ['$rootScope', '$http', function ($rootScope, $http) {
        "use strict";

    var proj4326 = new OpenLayers.Projection("EPSG:4326");
    var projmerc = new OpenLayers.Projection("EPSG:900913");

    /**
     * Converts from degrees to radians
     * @param degree the degrees
     * @returns the radians
     */
    function toRad(degree) {
        return degree / 360 * 2 * Math.PI;
    }

    /**
     * Converts from radians to degrees
     * @param radians the radians
     * @returns the degrees
     */
    function toDegree(rad) {
        return rad * 360 / 2 / Math.PI;
    }

    /**
     * Creates an OpenLayer point properly transformed
     * @param lon longitude
     * @param lat latitude
     * @returns the point
     */
    function createPoint(lon, lat) {
        return new OpenLayers.Geometry.Point(lon, lat).transform(proj4326, projmerc);
    }

    /**
     * Constructs a circular ring of transformed points
     * @param lon longitude
     * @param lat latitude
     * @param radius the radius in nm
     * @param noPoints the number of points
     * @returns the list of points that constitutes the ring
     */
    function calculateRing(lon, lat, radius, noPoints) {
        var points = [];
        var lat1 = toRad(lat);
        var lon1 = toRad(lon);
        var R = 6371.0087714; // earths mean radius
        var d = radius * 1852.0 / 1000.0; // nm -> km
        for (var i = 0; i < noPoints; i++) {
            var brng = Math.PI * 2 * i / noPoints;
            var lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) +
                Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
            var lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1),
                    Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));

            points.push(createPoint(toDegree(lon2), toDegree(lat2)));
        }
        return points;
    }


    // Return the public API
    return {

        /**
         * Creates the location OpenLayer feature
         * @param loc the location
         * @param attr the attributes to associate with the feature
         * @param features the feature list to update
         * @returns the updated feature list
         */
        createLocationFeature: function(loc, attr, features) {
            if (!loc || !loc.type) {
                return features;
            }

            // If the attr.bg attribute is set, it signals that the
            // feature is used to add a background "outilne" with a wider
            // stroke and different stroke color.
            // Since Points are shown as icons, ignore them.
            var isBG = (attr.bg && attr.bg == true);

            attr.locType = loc.type;

            switch (loc.type) {
                case 'POINT':
                    if (!isBG) {
                        for (var j in loc.points) {
                            features.push(new OpenLayers.Feature.Vector(createPoint(loc.points[j].lon, loc.points[j].lat), attr));
                        }
                    }
                    break;

                case "POLYGON":
                case "POLYLINE":
                    var points = [];
                    for (var j in loc.points) {
                        var p = loc.points[j];
                        points.push(createPoint(p.lon, p.lat));
                    }

                    features.push(new OpenLayers.Feature.Vector(
                        (loc.type == 'POLYGON')
                            ? new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)])
                            : new OpenLayers.Geometry.LineString(points),
                        attr
                    ));

                    if (attr.showVertices) {
                        for (var j in points) {
                            features.push(new OpenLayers.Feature.Vector(points[j], attr));
                        }
                    }
                    break;

                case 'CIRCLE':
                    var points = calculateRing(loc.points[0].lon, loc.points[0].lat, loc.radius, 40);
                    features.push(new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)]), attr));
                    break;
            }
            return features;
        },

        /**
         * Calls the back-end to parse a KML text
         * @param kml the kml text
         * @param success the success function
         * @param error the error function
         */
        parseKml: function(kml, success, error) {
            $http
                .post('/rest/location/parse-kml', kml)
                .success(function (data) {
                    success(data);
                })
                .error(function (data) {
                    error(data);
                });
        },

        /**
         * Zooms the map the the extent of the given layer
         */
        zoomToExtent : function(map, layer) {
            var extent = new OpenLayers.Bounds();

            var e = layer.getDataExtent();
            if (e) {
                extent.bottom = Math.min(99999999, e.bottom);
                extent.left = Math.min(99999999, e.left);
                extent.top = Math.max(-99999999, e.top);
                extent.right = Math.max(-99999999, e.right);

                var deltaV = extent.top - extent.bottom;
                var deltaH =  extent.right - extent.left;

                // Handle point case
                if (deltaH < 100 && deltaV < 100) {
                    deltaH = 30000;
                    deltaV = 30000;
                }

                extent.bottom -= deltaV * 0.1;
                extent.left -= deltaH * 0.1;
                extent.right += deltaH * 0.1;
                extent.top += deltaV * 0.1;

                map.zoomToExtent(extent);
            }
        },

        zoomToFeature : function(map, feature) {
            // If the feature is a point (or clost to it), select a greated extent
            var min = 10000;
            var extent = feature.geometry.getBounds();
            if (Math.abs(extent.bottom - extent.top) < 100 && Math.abs(extent.right - extent.left) < 100) {
                var size = 20000;
                extent.bottom -= size;
                extent.left -= size;
                extent.right += size;
                extent.top += size;
            }

            map.zoomToExtent(extent);
        },

        formatLocationsAsText : function(locations) {
            var txt = '';
            for (var l in locations) {
                var loc = locations[l];
                txt += "Type: " + loc.type + '\n';
                for (var d in loc.descs) {
                    txt += "[" + loc.descs[d].lang + "]: " + loc.descs[d].description + '\n';
                }
                if (loc.type == "CIRCLE") {
                    txt += "Radius: " + loc.radius + ' nm\n';
                }
                for (var p in loc.points) {
                    var pt = loc.points[p];
                    txt += formatLonLat(pt);
                    for (var pd in pt.descs) {
                        txt += ",[" + pt.descs[pd].lang + "]: " + pt.descs[pd].description;
                    }
                    txt += '\n';
                }
                txt += '\n';
            }
            return txt;
        },

        parseLocationsFromText : function(txt) {
            var locations = [];
            var lines = txt.split('\n');
            var loc = undefined;

            var desc = undefined;
            for (var l in lines) {
                var line = lines[l];

                // Check if this is the start of a new location
                var newLocMatch = line.toUpperCase().match(/^TYPE: (POINT|CIRCLE|POLYGON|POLYLINE)$/);
                if (newLocMatch) {
                    loc = { type: newLocMatch[1], descs: [], points: [] };
                    desc = undefined;
                    locations.push(loc);
                }

                // Check if the line is a location description
                var descMatch = line.match(/^\[(\w\w)\]:(.*)$/);
                if (loc && descMatch) {
                    desc = { lang: descMatch[1], description: descMatch[2].trim() };
                    loc.descs.push(desc);
                }

                // Check if the line is a radius
                var radiusMatch = line.match(/^Radius: (\d+)(.*)$/i);
                if (loc != null && radiusMatch) {
                    var radius = parseInt(radiusMatch[1]);
                    if (radiusMatch.length > 2 && radiusMatch[2].toUpperCase().trim() == "KM") {
                        radius = km2nm(radius);
                    }
                    loc.radius = radius;
                }

                // 55 04.424N  007 58.066E,[da]: bla bla bla,[en]: goobledygook
                var posMatch =  line.match(/^((\d{1,3}) (\d{1,2}(\.\d{1,3})?)(N|S))\s+((\d{1,3}) (\d{1,2}(\.\d{1,3})?)(E|W))(.*)$/i);
                if (loc != null && posMatch && posMatch.length > 10) {
                    var pt = { lat: parseLatitude(posMatch[1]), lon: parseLongitude(posMatch[6]), index: loc.points.length + 1, descs:[] };
                    loc.points.push(pt);

                    var descsMatch,
                        re = /(?:,\[(\w\w)\]:([^,\[]*))/g;
                    while (descsMatch = re.exec(posMatch[11])) {
                        desc = { lang: descsMatch[1], description: descsMatch[2].trim() };
                        pt.descs.push(desc);
                    }
                }

            }
            return locations;
        }

    }
}]);


