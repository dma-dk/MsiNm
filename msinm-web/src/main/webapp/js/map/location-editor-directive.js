

/**
 * Editor for creating and editing an list locations
 */
angular.module('msinm.map')
    .directive('msiLocationEditor', ['MapService', function (MapService) {
        'use strict';

        return {
            restrict: 'A',

            templateUrl: '/partials/common/location-editor.html',

            scope: {
                locations: '=locations'
            },

            link: function (scope, element, attrs) {

                scope.newPt = { lat: undefined, lon: undefined, description: undefined };

                scope.location = undefined;

                scope.deleteLocation = function(loc) {
                    scope.locations.splice(scope.locations.indexOf(loc), 1);
                };

                scope.deletePoint = function (pt) {
                    scope.location.points.splice(scope.location.points.indexOf(pt), 1);
                };

                scope.addPoint = function () {
                    if (!scope.newPt.lat || !scope.newPt.lon) {
                        return;
                    }
                    scope.newPt.index = scope.location.points.length + 1;
                    scope.location.points.push(angular.copy(scope.newPt));
                    scope.newPt = { lat: undefined, lon: undefined, description:undefined };
                };

                scope.toggleShowDesc = function (pt) {
                    pt.showDesc = !pt.showDesc;
                };

                scope.showLocationPanel = true;

                scope.toggleFadePanelInOut = function () {
                    if (scope.showLocationPanel) {
                        $('.location-editor-panel').fadeOut(200);
                    } else {
                        $('.location-editor-panel').fadeIn(200);
                    }
                    scope.showLocationPanel = !scope.showLocationPanel;
                };

                var quiescent = false;
                var zoom = attrs.zoom || 6;
                var lon = attrs.lon || 11;
                var lat = attrs.lat || 56;

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
                    div: angular.element(element.children()[0])[0],
                    theme: null,
                    layers: [
                        new OpenLayers.Layer.OSM("OpenStreetMap"),
                        locLayer
                    ],
                    units: "degrees",
                    projection: projmerc,
                    center: new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc),
                    zoom: zoom
                });

                /*********************************/
                /* Mouse location label          */
                /*********************************/
                map.events.register("mousemove", map, function (e) {
                    var point = map.getLonLatFromPixel(this.events.getMousePosition(e));
                    var pos = new OpenLayers.LonLat(point.lon, point.lat).transform(projmerc, proj4326);
                    scope.$apply(function () {
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

                for (var key in drawControls) {
                    map.addControl(drawControls[key]);
                }

                scope.deactivateDrawControls = function() {
                    for(var key in drawControls) {
                        drawControls[key].deactivate();
                    }
                };

                scope.addLocationType = function(value) {
                    scope.location = { type: value.toUpperCase(), points: [], description: '' };
                    if (value == 'point' || value == 'circle') {
                        scope.location.points.push({ lat: undefined, lon: undefined, index:1});
                    }
                    scope.locations.push(scope.location);

                    scope.deactivateDrawControls();
                    drawControls[value].activate();
                };

                /*********************************/
                /* Handle feature events         */
                /*********************************/
                locLayer.events.on({
                    "featureadded": function (evt) {
                        if (quiescent) {
                            return;
                        }

                        var points = [];
                        if (drawControls.point.active) {
                            var pt = evt.feature.geometry.transform(projmerc, proj4326);
                            points.push({ lat: pt.y, lon: pt.x, index:1});

                        } else if (drawControls.circle.active) {
                            var center = evt.feature.geometry.getBounds().getCenterLonLat();
                            var line = new OpenLayers.Geometry.LineString([
                                evt.feature.geometry.getVertices()[0],
                                new OpenLayers.Geometry.Point(center.lon, center.lat)]);
                            var radius = Math.round(line.getGeodesicLength(projmerc) / 1000);
                            var pt = center.transform(projmerc, proj4326);
                            scope.location.radius = radius;
                            points.push({ lat: pt.lat, lon: pt.lon , index:1 });

                        } else {
                            var vertices = evt.feature.geometry.getVertices();
                            var pt, num = 0;
                            for (var i in  vertices) {
                                pt = vertices[i].transform(projmerc, proj4326);
                                points.push({ lat: pt.y, lon: pt.x , index:num++ });
                            }
                        }
                        scope.location.points = points;

                        scope.deactivateDrawControls();

                        if(!scope.$$phase) {
                            scope.$apply();
                        }
                    }
                });


                /*********************************/
                /* Handle changed location       */
                /*********************************/
                scope.$watch(attrs.locations, function (value) {

                    locLayer.removeAllFeatures();
                    quiescent = true;
                    if (value) {
                        for (var key in value) {
                            var loc = value[key];
                            var features = [];
                            try {
                                var attr = {
                                    id: 1,
                                    description: "location filter",
                                    type: "loc"
                                };

                                MapService.createLocationFeature(loc, attr, features);
                                locLayer.addFeatures(features);
                            } catch (ex) {
                                console.log("Error: " + ex);
                            }
                        }
                    }
                    quiescent = false;
                }, true);
            }
        }
    }]);

