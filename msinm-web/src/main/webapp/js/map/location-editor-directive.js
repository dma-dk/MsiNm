

/**
 * Editor for creating and editing an list locations
 */
angular.module('msinm.map')
    .directive('msiLocationEditor', ['$modal', 'LangService', 'MapService', function ($modal, LangService, MapService) {
        'use strict';

        return {
            restrict: 'A',

            transclude: true,

            templateUrl: '/partials/common/location-editor.html',

            scope: {
                locations: '=locations',
                visible: '=visible',
                editDescs: '@editDescs'
            },

            link: function (scope, element, attrs) {

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
                    displayInLayerSwitcher: false,
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

                // Build the array of layers to include
                var layers = [];
                // addBaseMapLayers() will add the base layers configured in conf/base-layers.js
                addBaseMapLayers(layers);
                // Add the mandatory layers
                layers.push(locLayer);

                /*********************************/
                /* Map                           */
                /*********************************/
                var map = new OpenLayers.Map({
                    div: angular.element(element.children()[0])[0],
                    theme: null,
                    layers: layers,
                    units: "degrees",
                    projection: projmerc,
                    center: new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc),
                    zoom: zoom
                });

                map.addControl(new OpenLayers.Control.LayerSwitcher({
                    'div' : OpenLayers.Util.getElement('location-layerswitcher')
                }));

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

                // Get the OpenLayers feature associated with the given location
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

                // Select the feature of the given location for modificaiton
                function selectLocation(loc) {
                    var feature = getLocationFeature(loc);
                    if (feature) {
                        drawControls.modify.selectFeature(feature);
                    }
                }

                // Called when "featureadded" or "featuremodified" is fired in the location layer.
                // Converts the OpenLayers fetatures into the location model.
                // There's also an attempt to restore the description records from the old model.
                function readFeatureGeometry(evt) {
                    if (quiescent) {
                        return;
                    }

                    var points = [];
                    if (scope.location.type == 'POINT') {
                        var pt = evt.feature.geometry.transform(projmerc, proj4326);
                        var oldPt = (scope.location.points.length > 0) ? scope.location.points[0] : undefined;
                        points.push(initDescs({ lat: pt.y, lon: pt.x, index:1}, oldPt));

                    } else if (scope.location.type == 'CIRCLE') {
                        var center = evt.feature.geometry.getBounds().getCenterLonLat();
                        var line = new OpenLayers.Geometry.LineString([
                            evt.feature.geometry.getVertices()[0],
                            new OpenLayers.Geometry.Point(center.lon, center.lat)]);
                        var radius = m2nm(line.getGeodesicLength(projmerc));
                        var pt = center.transform(projmerc, proj4326);
                        scope.location.radius = radius;
                        var oldPt = (scope.location.points.length > 0) ? scope.location.points[0] : undefined;
                        points.push(initDescs({ lat: pt.lat, lon: pt.lon , index:1}, oldPt));

                    } else {
                        var vertices = evt.feature.geometry.getVertices();
                        var pt, num = 0;
                        for (var i in  vertices) {
                            pt = vertices[i].transform(projmerc, proj4326);
                            // NB: Finding the old point by index is actually not correct if a point has been added or deleted
                            var oldPt = (scope.location.points.length > i) ? scope.location.points[i] : undefined;
                            points.push(initDescs({ lat: pt.y, lon: pt.x , index:num++}, oldPt));
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

                // Triggers when the model has been changed
                scope.$watch(function () {
                    return scope.locations;
                }, function (value) {
                    locLayer.removeAllFeatures();

                    // Adding OpenLayers features based on the location model
                    // will actually cause "featureadded" events. Setting the
                    // "quiescent" will stop us from converting features back
                    // into a model upon receiving this event.
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

                // Called when the location editor becomes (in-)visible
                if (attrs.visible) {
                    scope.$watch(function () {
                        return scope.visible;
                    }, function (newValue) {
                        if (newValue) {
                            // The location editor is being displayed
                            // Get OpenLayers to update it's size
                            map.updateSize();

                            // Update the locations by ensuring that all locations and points
                            // have all language-specific description entities, and by
                            // computing the "showDesc" attribute of each location and point
                            initLocations();
                        }
                    }, true);
                }

                /*********************************/
                /* I18N Support                  */
                /*********************************/

                // Used to ensure that description entities have a "description" field
                function ensureDescriptionField(desc) {
                    desc.description = '';
                }

                // Set the "showDesc" flag true for all locations and points
                // that have non-empty descriptions
                function initLocations() {
                    for (var i in scope.locations) {
                        var loc = initDescs(scope.locations[i], undefined, true);
                        for (var p in loc.points) {
                            initDescs(loc.points[p], undefined, true);
                        }

                        // While we are at it, add a newPt to the locations
                        loc.newPt = initDescs({ lat: undefined, lon: undefined });
                    }
                }

                // Check if any of the descriptions are defined
                function hasDesc(descs) {
                    for (var d in descs) {
                        if (descs[d].description && descs[d].description.length > 0) {
                            return true;
                        }
                    }
                    return false;
                }

                // Add a localized desc entity with an existing or empty "description" attribute for each model language.
                // If oldElm (location or point) is defined, the existing desc entities are used.
                // Otherwise, if computeShowDesc is true, the "showDesc" flag is computed for the element
                function initDescs(elm, oldElm, computeShowDesc) {
                    LangService.checkDescs(elm, ensureDescriptionField, oldElm);

                    // Restore the "showDesc" flag from the oldElm
                    if (oldElm && oldElm.showDesc) {
                        elm.showDesc = oldElm.showDesc;
                    } else if (computeShowDesc) {
                        elm.showDesc = hasDesc(elm.descs);
                    }
                    return elm;
                }

                /*********************************/
                /* Location actions              */
                /*********************************/

                // Zooms the map to the given location
                scope.zoomToLocation = function(loc) {
                    var feature = getLocationFeature(loc);
                    if (feature) {
                        MapService.zoomToFeature(map, feature);
                        drawControls.modify.selectFeature(feature);
                    }
                };

                // Deletes the given location
                scope.deleteLocation = function(loc) {
                    scope.locations.splice(scope.locations.indexOf(loc), 1);
                };

                // Deletes the given point
                scope.deletePoint = function (loc, pt) {
                    loc.points.splice(loc.points.indexOf(pt), 1);
                };

                // Adds a new point to the current location
                scope.addPoint = function (loc, pt) {
                    if (!pt || !pt.lat || !pt.lon) {
                        return;
                    }
                    pt.index = loc.points.length + 1;
                    loc.points.push(angular.copy(pt));
                    loc.newPt = initDescs({ lat: undefined, lon: undefined, descs:[] });
                };

                /*********************************/
                /* Button panel actions          */
                /*********************************/

                // Toggle show/hide the description fields of the given point or location
                scope.toggleShowDesc = function (elm) {
                    elm.showDesc = (elm.showDesc) ? !elm.showDesc : true;
                };

                // Adds a new empty location of the given type
                scope.addLocationType = function(value) {
                    scope.location = initDescs({ type: value.toUpperCase(), points: [], descs:[] });
                    if (value == 'point' || value == 'circle') {
                        scope.location.points.push({ lat: undefined, lon: undefined, index:1});
                    }
                    scope.locations.push(scope.location);

                    scope.deactivateDrawControls(false);
                    drawControls[value].activate();
                };

                // Adds the current location with a radius of 10 NM
                scope.addCurrentLocation = function () {
                    navigator.geolocation.getCurrentPosition(function(pos) {
                        scope.location = initDescs({
                            type: 'CIRCLE',
                            radius: 10,
                            points: [{lat: pos.coords.latitude, lon: pos.coords.longitude, index: 1 }],
                            descs:[] });
                        scope.locations.push(scope.location);
                        scope.deactivateDrawControls(false);
                        if(!scope.$$phase) {
                            scope.$apply();
                        }
                    });
                };

                // Show/hide the panel that lists the locations
                scope.toggleShowLocationPanel = function () {
                    if (scope.showLocationPanel) {
                        $('.location-editor-locations').fadeOut(100);
                    } else {
                        $('.location-editor-locations').fadeIn(100);
                    }
                    scope.showLocationPanel = !scope.showLocationPanel;
                };

                // Clear all locations
                scope.clearLocations = function() {
                    scope.locations.splice(0, scope.locations.length);
                };

                // Zoom to the extent of the location layer
                scope.zoomToExtent = function() {
                    MapService.zoomToExtent(map, locLayer);
                };

                // Provides an option to edit the locations as plain text
                scope.editAsText = function() {
                    scope.modalInstance = $modal.open({
                        templateUrl: "/partials/common/location-editor-text-edit.html",
                        controller: function ($scope) {
                            $scope.locationsTxt = MapService.formatLocationsAsText(scope.locations);
                        }
                    });

                    // Parse the location text from the textarea
                    scope.modalInstance.result.then(function(result) {
                        var data = MapService.parseLocationsFromText(result);
                        if (data) {
                            scope.clearLocations();
                            for (var i in data) {
                                scope.locations.push(data[i]);
                            }
                            initLocations();
                            scope.deactivateDrawControls(true);
                        }
                    }, function() {
                        // Cancelled
                    })['finally'](function(){
                        scope.modalInstance = undefined;
                    });
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

