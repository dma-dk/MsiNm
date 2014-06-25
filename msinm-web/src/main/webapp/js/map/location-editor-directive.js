

/**
 * Editor for creating and editing an list locations
 */
angular.module('msinm.map')
    .directive('msiLocationEditor', ['$modal', '$rootScope', 'MapService', function ($modal, $rootScope, MapService) {
        'use strict';

        return {
            restrict: 'A',

            transclude: true,

            templateUrl: '/partials/common/location-editor.html',

            scope: {
                locations: '=locations',
                visible: '=visible'
            },

            link: function (scope, element, attrs) {

                // If oldElm (location or point) is defined, look for a description with the given language
                function descForLang(lang, oldElm) {
                    if (oldElm && oldElm.descs) {
                        for (var d in oldElm.descs) {
                            if (oldElm.descs[d].lang == lang) {
                                return oldElm.descs[d];
                            }
                        }
                    }
                }

                // Add a localized desc entity with an existing or  empty "description" attribute for each model language.
                // If oldElm (location or point) is defined, the existing desc entities are used.
                function initDescs(elm, oldElm) {
                    for (var i in $rootScope.modelLanguages) {
                        var desc = descForLang($rootScope.modelLanguages[i], oldElm);
                        if (!desc) {
                            desc = { lang: $rootScope.modelLanguages[i], description: undefined };
                        }
                        elm.descs.push(desc);
                    }
                    if (oldElm && oldElm.showDesc) {
                        elm.showDesc = oldElm.showDesc;
                    }
                    return elm;
                }

                scope.newPt = initDescs({ lat: undefined, lon: undefined, descs: [] });

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
                            fillOpacity: 0.2,
                            pointRadius: 6,
                            strokeWidth: 2,
                            strokeColor: "#080",
                            strokeOpacity: 0.6
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
                drawControls.modify.activate();

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

                function getLocationFeature(loc) {
                    if (loc) {
                        for (var i = 0; i < locLayer.features.length; ++i) {
                            if (locLayer.features[i].data.location == loc) {
                                return locLayer.features[i];
                            }
                        }
                    }
                    return null;
                }

                function selectLocation(loc) {
                    var feature = getLocationFeature(loc);
                    if (feature) {
                        drawControls.modify.selectFeature(feature);
                    }
                }

                function readFeatureGeometry(evt) {
                    if (quiescent) {
                        return;
                    }

                    var points = [];
                    if (scope.location.type == 'POINT') {
                        var pt = evt.feature.geometry.transform(projmerc, proj4326);
                        var oldPt = (scope.location.points.length > 0) ? scope.location.points[0] : undefined;
                        points.push(initDescs({ lat: pt.y, lon: pt.x, index:1, descs: []}, oldPt));

                    } else if (scope.location.type == 'CIRCLE') {
                        var center = evt.feature.geometry.getBounds().getCenterLonLat();
                        var line = new OpenLayers.Geometry.LineString([
                            evt.feature.geometry.getVertices()[0],
                            new OpenLayers.Geometry.Point(center.lon, center.lat)]);
                        var radius = Math.round(line.getGeodesicLength(projmerc) / 1000);
                        var pt = center.transform(projmerc, proj4326);
                        scope.location.radius = radius;
                        var oldPt = (scope.location.points.length > 0) ? scope.location.points[0] : undefined;
                        points.push(initDescs({ lat: pt.lat, lon: pt.lon , index:1, descs: []}, oldPt));

                    } else {
                        var vertices = evt.feature.geometry.getVertices();
                        var pt, num = 0;
                        for (var i in  vertices) {
                            pt = vertices[i].transform(projmerc, proj4326);
                            // NB: Finding the old point by index is actually not correct if a point has been added or deleted
                            var oldPt = (scope.location.points.length > i) ? scope.location.points[i] : undefined;
                            points.push(initDescs({ lat: pt.y, lon: pt.x , index:num++, descs: []}, oldPt));
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
                scope.$watch(function () {
                    return scope.locations;
                }, function (value) {
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
                /* Handle changed visibility     */
                /*********************************/
                if (attrs.visible) {
                    scope.$watch(function () {
                        return scope.visible;
                    }, function (newValue) {
                        if (newValue) {
                            map.updateSize();
                        }
                    }, true);
                }

                /*********************************/
                /* Location actions              */
                /*********************************/

                scope.zoomToLocation = function(loc) {
                    var feature = getLocationFeature(loc);
                    if (feature) {
                        MapService.zoomToFeature(map, feature);
                        drawControls.modify.selectFeature(feature);
                    }
                };

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
                    scope.newPt = initDescs({ lat: undefined, lon: undefined, descs:[] });
                };

                /*********************************/
                /* Button panel actions          */
                /*********************************/
                scope.toggleShowDesc = function (pt) {
                    pt.showDesc = !pt.showDesc;
                };

                scope.addLocationType = function(value) {
                    scope.location = initDescs({ type: value.toUpperCase(), points: [], descs:[] });
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

