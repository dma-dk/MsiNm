

/**
 * Editor for creating and editing an list locations
 */
angular.module('msinm.map')
    .directive('msiLocationEditor', ['$modal', 'MapService', function ($modal, MapService) {
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

                scope.showLocationPanel = true;

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
                        }),
                    modify: new OpenLayers.Control.ModifyFeature(locLayer, {
                        mode: OpenLayers.Control.ModifyFeature.RESHAPE | OpenLayers.Control.ModifyFeature.DRAG
                    })
                };

                for (var key in drawControls) {
                    map.addControl(drawControls[key]);
                }

                scope.deactivateDrawControls = function(modify) {
                    for(var key in drawControls) {
                        drawControls[key].deactivate();
                    }
                    if (modify) {
                        drawControls.modify.activate();
                    }
                };

                /*********************************/
                /* Handle feature events         */
                /*********************************/
                locLayer.events.on({
                    "featureclick": function (evt) {
                        // TODO: Can we change modify options for circles to only support drag?
                        if (evt.feature.data.location) {
                            scope.location = evt.feature.data.location;
                        }
                    },

                    "featuremodified": function (evt) {
                        if (evt.feature.data && evt.feature.data.location) {
                            scope.location = evt.feature.data.location;
                            readFeatureGeometry(evt);
                            selectLocation(scope.location);
                        }
                    },

                    "featureadded": function (evt) {
                        readFeatureGeometry(evt);
                        if (!quiescent) {
                            scope.deactivateDrawControls(true);
                        }
                    }
                });

                function selectLocation(loc) {
                    if (loc) {
                        for (var i = 0; i < locLayer.features.length; ++i) {
                            if (locLayer.features[i].data.location == loc) {
                                drawControls.modify.selectFeature(locLayer.features[i]);
                            }
                        }
                    }
                }

                function readFeatureGeometry(evt) {
                    if (quiescent) {
                        return;
                    }

                    var points = [];
                    if (scope.location.type == 'POINT') {
                        var pt = evt.feature.geometry.transform(projmerc, proj4326);
                        points.push({ lat: pt.y, lon: pt.x, index:1});

                    } else if (scope.location.type == 'CIRCLE') {
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

                    if(!scope.$$phase) {
                        scope.$apply();
                    }
                }


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
                                    type: "loc",
                                    location: loc
                                };

                                MapService.createLocationFeature(loc, attr, features);
                                locLayer.addFeatures(features);
                            } catch (ex) {
                                console.error("Error: " + ex);
                            }
                        }
                    }
                    quiescent = false;
                }, true);

                /*********************************/
                /* Location actions              */
                /*********************************/
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

                /*********************************/
                /* Button panel actions          */
                /*********************************/
                scope.toggleShowDesc = function (pt) {
                    pt.showDesc = !pt.showDesc;
                };

                scope.addLocationType = function(value) {
                    scope.location = { type: value.toUpperCase(), points: [], description: '' };
                    if (value == 'point' || value == 'circle') {
                        scope.location.points.push({ lat: undefined, lon: undefined, index:1});
                    }
                    scope.locations.push(scope.location);

                    scope.deactivateDrawControls(false);
                    drawControls[value].activate();
                };

                scope.toggleShowLocationPanel = function () {
                    if (scope.showLocationPanel) {
                        $('.location-editor-locations').fadeOut(100);
                    } else {
                        $('.location-editor-locations').fadeIn(100);
                    }
                    scope.showLocationPanel = !scope.showLocationPanel;
                };

                scope.clearLocations = function() {
                    scope.locations.splice(0, scope.locations.length);
                };

                scope.zoomToExtent = function() {
                    MapService.zoomToExtent(map, locLayer);
                };

                // Open modal dialog to import via KML by pasting it into a text area
                // or upload a .kmz file.
                scope.importLocations = function() {

                    scope.modalInstance = $modal.open({
                        templateUrl : "/partials/common/location-editor-import.html",
                        controller: function ($scope) {
                            // Callback for when a .kmz has been uploaded
                            $scope.kmzFileUploaded = function(result) {
                                $scope.$close();

                                scope.clearLocations();
                                for (var i in result) {
                                    scope.locations.push(result[i]);
                                }
                                scope.deactivateDrawControls(true);
                                if(!scope.$$phase) {
                                    scope.$apply();
                                }
                            };
                        }
                    });

                    // Get the KML pasted into a textarea
                    scope.modalInstance.result.then(function(result) {
                        var kml = result;
                        if (kml) {
                            MapService.parseKml(
                                kml,
                                function (data) {
                                    scope.clearLocations();
                                    for (var i in data) {
                                        scope.locations.push(data[i]);
                                    }
                                    scope.deactivateDrawControls(true);
                                },
                                function (data) {
                                    console.error("Error: " + data);
                                }
                            );
                        }
                    }, function() {
                        // Cancelled
                    })['finally'](function(){
                        scope.modalInstance = undefined;
                    });
                };
            }
        }
    }]);

