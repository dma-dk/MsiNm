
/**
 * Converts a div into a search result map
 */
angular.module('msinm.map')
    .directive('msiSearchResultMap', ['$modal', 'MapService', function ($modal, MapService) {
    'use strict';

    return {
        restrict: 'A',

        link: function (scope, element, attrs) {

            var zoom    = attrs.zoom || 6;
            var lon     = attrs.lon || 11;
            var lat     = attrs.lat || 56;

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
                        fillOpacity: 0.1,
                        pointRadius: 6,
                        strokeWidth: 2,
                        strokeColor: "#080",
                        strokeOpacity: 0.3
                    })
                })
            });

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
            layers.push(locLayer);

            /*********************************/
            /* Map                           */
            /*********************************/

            var map = new OpenLayers.Map({
                div: element[0],
                theme: null,
                layers: layers,
                units : "degrees",
                projection : projmerc,
                center: new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc),
                zoom: zoom
            });

            map.addControl(new OpenLayers.Control.LayerSwitcher({
                'div' : OpenLayers.Util.getElement('search-layerswitcher')
            }));

            /*********************************/
            /* Update the location feature   */
            /*********************************/
            scope.$watch(attrs.locations, function (value) {

                locLayer.removeAllFeatures();
                if (value && value.length > 0) {
                    var features = [];
                    try {
                        var attr = {
                            id: 1,
                            description: "location filter",
                            type: "loc"
                        };
                        for (var i in value) {
                            var location = value[i];
                            MapService.createLocationFeature(location, attr, features);
                        }
                        locLayer.addFeatures(features);
                    } catch (ex) {
                        console.log("Error: " + ex);
                    }
                }
            },true);


            /*********************************/
            /* Update MSI and NtM's          */
            /*********************************/
            scope.$watch(attrs.searchResult, function (value) {

                msiLayer.removeAllFeatures();
                if (value && value.messages)  {
                    var features = [];

                    for (var i in value.messages) {
                        var msg = value.messages[i];
                        var icon = "img/" + msg.seriesIdentifier.mainType.toLowerCase() + ".png";

                        for (var j in msg.locations) {
                            var loc = msg.locations[j];

                            var title = (msg.descs && msg.descs.length > 0) ? msg.descs[0].title : "N/A";
                            var bgAttr = { id : i, description: title, type : "msi", msi : msg, icon: icon, bg:true  };
                            MapService.createLocationFeature(loc, bgAttr, features);

                            // Flick the "showVertices to true to show icons for each vertex
                            var attr = { id : i, description: title, type : "msi", msi : msg, icon: icon, showVertices:false  };
                            MapService.createLocationFeature(loc, attr, features);
                        }
                    }
                    msiLayer.addFeatures(features);
                }
            });

            /*********************************/
            /* Pop-ups for the features      */
            /*********************************/
            var msiSelect = new OpenLayers.Control.SelectFeature(msiLayer);
            msiLayer.events.on({
               "featureselected": onMsiSelect
            });
            map.addControl(msiSelect);
            msiSelect.activate();

            function onPopupClose(evt) {
                msiSelect.unselectAll();
            }

            function onMsiSelect(event) {

                $modal.open({
                    controller: "MessageCtrl",
                    templateUrl: "/partials/search/message-details.html",
                    size: 'lg',
                    resolve: {
                        messageId: function () {
                            return event.feature.attributes.msi.id;
                        }
                    }
                });
            }

        }
    }
}]);


