
/**
 * MapService factory
 */
angular.module('msinm.map')
       .factory('MapService', function ($rootScope) {
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
     * @param radius the radius in km
     * @param noPoints the number of points
     * @returns the list of points that constitutes the ring
     */
    function calculateRing(lon, lat, radius, noPoints) {
        var points = [];
        var lat1 = toRad(lat);
        var lon1 = toRad(lon);
        var R = 6371.0087714; // earths mean radius
        var d = radius;
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

            switch (loc.type) {
                case 'POINT':
                    for (var j in loc.points) {
                        features.push(new OpenLayers.Feature.Vector(createPoint(loc.points[j].lon, loc.points[j].lat), attr));
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
                    break;

                case 'CIRCLE':
                    var points = calculateRing(loc.points[0].lon, loc.points[0].lat, loc.radius, 40);
                    features.push(new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)]), attr));
                    break;
            }
            return features;
        }
    }
});

