

/**
 * Map that displays the MSI and NM locations of a message
 */
angular.module('msinm.map')
    .directive('msiMessageDetailsMap', ['$timeout', 'LangService', 'MapService', function ($timeout, LangService, MapService) {
        'use strict';

        return {
            restrict: 'A',

            transclude: true,

            templateUrl: '/partials/common/message-details-map.html',

            scope: {
                msiMessageDetailsMap: '='
            },

            link: function (scope, element, attrs) {

                var zoom = attrs.zoom || 6;
                var lon = attrs.lon || 11;
                var lat = attrs.lat || 56;

                var proj4326 = new OpenLayers.Projection("EPSG:4326");
                var projmerc = new OpenLayers.Projection("EPSG:900913");

                /*********************************/
                /* Layers                        */
                /*********************************/
                var msiContext = {
                    strokeWidth: function(feature) {
                        return feature.attributes.bg ? 4 : 2;
                    },
                    strokeColor: function(feature) {
                        return feature.attributes.bg ? "#ffffff" : "#8f2f7b";
                    },
                    fillColor: function(feature) {
                        return feature.attributes.bg ? "#ffffff" : "#ad57a1";
                    },
                    graphicSize: function(feature) {
                        return 20;
                    },
                    graphicOffset: function(feature) {
                        return -msiContext.graphicSize() / 2;
                    }
                };

                var msiLayer  = new OpenLayers.Layer.Vector( "Msi-Nm", {
                    displayInLayerSwitcher: false,
                    styleMap: new OpenLayers.StyleMap({
                        "default": new OpenLayers.Style({
                            externalGraphic : "${icon}",
                            graphicWidth : "${graphicSize}",
                            graphicHeight : "${graphicSize}",
                            graphicYOffset : "${graphicOffset}",
                            graphicXOffset : "${graphicOffset}",
                            fillColor: "${fillColor}",
                            fillOpacity: 1.0,
                            pointRadius: 8,
                            strokeWidth: "${strokeWidth}",
                            strokeColor: "${strokeColor}",
                            strokeOpacity: 1.0
                        }, { context: msiContext })
                    })
                });

                // Build the array of layers to include
                var layers = [];
                // addBaseMapLayers() will add the base layers configured in conf/base-layers.js
                addBaseMapLayers(layers);
                // Add the mandatory layers
                layers.push(msiLayer);

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

                // Trigger an update of the map size
                $timeout(function() {
                    map.updateSize();
                }, 100);

                map.addControl(new OpenLayers.Control.LayerSwitcher({
                    'div' : OpenLayers.Util.getElement('message-details-layerswitcher')
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
                /* Handle changed location       */
                /*********************************/

                // Triggers when the model has been changed
                scope.$watch(function () {
                    return scope.msiMessageDetailsMap.locations;
                }, function (value) {
                    msiLayer.removeAllFeatures();

                    var msg = scope.msiMessageDetailsMap;
                    var icon = "img/" + msg.seriesIdentifier.mainType.toLowerCase() + ".png";

                    var features = [];
                    for (var j in msg.locations) {
                        var loc = msg.locations[j];

                        var title = (msg.descs && msg.descs.length > 0) ? msg.descs[0].title : "N/A";
                        var bgAttr = { description: title, type : "msi", msi : msg, icon: icon, bg:true  };
                        MapService.createLocationFeature(loc, bgAttr, features);

                        // Flick the "showVertices to true to show icons for each vertex
                        var attr = { description: title, type : "msi", msi : msg, icon: icon, showVertices:false  };
                        MapService.createLocationFeature(loc, attr, features);
                    }
                    msiLayer.addFeatures(features);

                    MapService.zoomToExtent(map, msiLayer);
                }, true);

            }
        }
    }]);

