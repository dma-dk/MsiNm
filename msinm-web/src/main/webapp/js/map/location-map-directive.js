
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

            var quiescent = false;
            var zoom    = attrs.zoom || 6;
            var lon     = attrs.lon || 11;
            var lat     = attrs.lat || 56;

            var proj4326 = new OpenLayers.Projection("EPSG:4326");
            var projmerc = new OpenLayers.Projection("EPSG:900913");

            /*********************************/
            /* Layers                        */
            /*********************************/
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

            /*********************************/
            /* Map                           */
            /*********************************/
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

            /*********************************/
            /* Mouse location label          */
            /*********************************/
            map.events.register("mousemove", map, function(e) {
                var point = map.getLonLatFromPixel( this.events.getMousePosition(e) );
                var pos = new OpenLayers.LonLat(point.lon, point.lat).transform(projmerc, proj4326);
                scope.$apply(function() {
                    scope.mousePos = formatLonLat(pos);
                })
            });

            /*********************************/
            /* Draw controls                 */
            /*********************************/
            var drawControls = {
                point: new OpenLayers.Control.DrawFeature(locLayer,
                    OpenLayers.Handler.Point),
                polyline: new OpenLayers.Control.DrawFeature(locLayer,
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

            /*********************************/
            /* Handle feature events         */
            /*********************************/
            locLayer.events.on({
                "beforefeatureadded": function (evt) {
                    if (locLayer.features.length > 0) {
                        locLayer.removeAllFeatures();
                    }
                },

                "featureadded": function (evt) {

                    var loc = '';
                    if (drawControls.point.active) {
                        var pt = evt.feature.geometry.transform(projmerc, proj4326);
                        loc = { type: "POINT", points: [{ lat: pt.y, lon: pt.x, index:1}]};

                    } else if (drawControls.circle.active) {
                        var center = evt.feature.geometry.getBounds().getCenterLonLat();
                        var line = new OpenLayers.Geometry.LineString([
                            evt.feature.geometry.getVertices()[0],
                            new OpenLayers.Geometry.Point(center.lon, center.lat)]);
                        var radius = Math.round(line.getGeodesicLength(projmerc) / 1000);
                        var pt = center.transform(projmerc, proj4326);
                        loc = { type: "CIRCLE", radius: radius, points: [{ lat: pt.lat, lon: pt.lon , index:1 }]};

                    } else {
                        var type = (drawControls.polyline.active) ? "POLYLINE" : "POLYGON";
                        var loc = { "type": type, points: [] };
                        var points = evt.feature.geometry.getVertices();
                        var num = 0;
                        for (var i in  points) {
                            var pt = points[i].transform(projmerc, proj4326);
                            loc.points[num] = { lat: pt.y, lon: pt.x, index: (num++) };
                        }
                    }

                    if (!quiescent) {
                        console.log("Setting loc " + loc);
                        scope.loc = loc;
                        if(!scope.$$phase) {
                            scope.$apply();
                        }
                    }
                }
            });


            /*********************************/
            /* Handle changed tool           */
            /*********************************/
            scope.$watch(attrs.tool, function (value) {
                for(var key in drawControls) {
                    drawControls[key].deactivate();
                }
                if (value == 'point' || value == 'circle' || value == 'polygon' || value == 'polyline') {
                    drawControls[value].activate();
                }
            });


            /*********************************/
            /* Handle changed location       */
            /*********************************/
            scope.$watch(attrs.loc, function (value) {

                locLayer.removeAllFeatures();
                if (value && value != '') {
                    var features = [];
                    quiescent = true;
                    try {
                        var attr = {
                            id: 1,
                            description: "location filter",
                            type: "loc"
                        };

                        MapService.createLocationFeature(value, attr, features);
                        locLayer.addFeatures(features);
                    } catch (ex) {
                        console.log("Error: " + ex);
                    }
                    quiescent = false;
                }
            }, true);

        }
    }
}]);


