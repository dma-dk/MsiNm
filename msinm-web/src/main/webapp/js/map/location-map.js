
/**
 * Directive to use as a <div> attribute to turn the div into
 * an OpenLayers map with a location indication.
 *
 */
angular.module('msinm.map')
       .directive('msiMap', ['MapService', function (MapService) {
        'use strict';

    return {
        restrict: 'A',

        link: function (scope, element, attrs) {

            var zoom    = attrs.zoom || 6;
            var lon     = attrs.lon || 11;
            var lat     = attrs.lat || 56;

            var proj4326 = new OpenLayers.Projection("EPSG:4326");
            var projmerc = new OpenLayers.Projection("EPSG:900913");

            var locLayer = new OpenLayers.Layer.Vector("Location", {
                styleMap: new OpenLayers.StyleMap({
                    "default": new OpenLayers.Style({
                        fillColor: "#080",
                        fillOpacity: 0.1,
                        pointRadius: 6,
                        strokeWidth: 2,
                        strokeColor: "#080",
                        strokeOpacity: 0.3
                    })
                })
            });

            var map = new OpenLayers.Map({
                div: element[0],
                theme: null,
                layers: [
                    new OpenLayers.Layer.OSM("OpenStreetMap"),
                    locLayer
                ],
                units : "degrees",
                projection : projmerc,
                center: new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc),
                zoom: zoom
            });

            var drawControls = {
                point: new OpenLayers.Control.DrawFeature(locLayer,
                    OpenLayers.Handler.Point),
                line: new OpenLayers.Control.DrawFeature(locLayer,
                    OpenLayers.Handler.Path),
                polygon: new OpenLayers.Control.DrawFeature(locLayer,
                    OpenLayers.Handler.Polygon),
                box: new OpenLayers.Control.DrawFeature(locLayer,
                    OpenLayers.Handler.RegularPolygon, {
                        handlerOptions: { sides: 4, irregular: true }
                    }),
                circle: new OpenLayers.Control.DrawFeature(locLayer,
                    OpenLayers.Handler.RegularPolygon, {
                        handlerOptions: {sides: 40}
                    })
            };

            for(var key in drawControls) {
                map.addControl(drawControls[key]);
            }

            drawControls.circle.activate();


            locLayer.events.on({
                "featureadded": function (evt) {

                    var loc = '';
                    if (drawControls.point.active) {
                        var pt = evt.feature.geometry.transform(projmerc, proj4326);
                        loc = '{"type":"POINT", "points":[{"lat":' + pt.y + ',"lon":' + pt.x + ',"num":1}]}';

                    } else if (drawControls.circle.active) {
                        var center = evt.feature.geometry.getBounds().getCenterLonLat();
                        var line = new OpenLayers.Geometry.LineString([
                            evt.feature.geometry.getVertices()[0],
                            new OpenLayers.Geometry.Point(center.lon, center.lat)]);
                        var radius = Math.round(line.getGeodesicLength(projmerc) / 1000);
                        var pt = center.transform(projmerc, proj4326);
                        loc = '{"type":"CIRCLE", "radius":' + radius + ', "points":[{"lat":' + pt.lat + ',"lon":' + pt.lon + ',"num":1}]}';

                    } else {
                        var type = (drawControls.line.active) ? "POLYLINE" : "POLYGON";
                        var loc = '{"type":"' + type + '", "points":[';
                        var points = evt.feature.geometry.getVertices();
                        var num = 1;
                        for (var i in  points) {
                            if (num > 1) {
                                loc = loc + ', ';
                            }
                            var pt = points[i].transform(projmerc, proj4326);
                            loc = loc + '{"lat":' + pt.y + ',"lon":' + pt.x + ',"num":' + (num++) + '}'
                        }
                        loc += ']}';
                    }
                    console.log("Setting loc " + loc);
                    $("#messageLocation").val(loc);
                    $("#messageLocation").trigger('input');
                    //attrs.loc = loc;
                }
            });



            if (attrs.loc) {

                scope.$watch(attrs.loc, function (value) {

                    locLayer.removeAllFeatures();
                    if (value && value != '') {
                        var features = [];
                        try {
                            var loc = JSON.parse(value);

                            var attr = {
                                id: 1,
                                description: "location filter",
                                type: "loc"
                            }

                            MapService.createLocationFeature(loc, attr, features);
                            locLayer.addFeatures(features);
                        } catch (ex) {
                            console.log("Error: " + ex);
                        }
                    }
                });
            }

        }
    }
}]);


