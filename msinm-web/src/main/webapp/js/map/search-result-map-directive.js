
/**
 * Converts a div into a search result map
 */
angular.module('msinm.map')
    .directive('msiSearchResultMap', ['$rootScope', '$location', 'MapService', function ($rootScope, $location, MapService) {
    'use strict';

    return {
        restrict: 'A',

        scope: {
            searchResult: '=',
            searchLocations: '&'
        },

        link: function (scope, element, attrs) {

            var zoom = attrs.zoom || $rootScope.DEFAULT_ZOOM_LEVEL;
            var lon  = attrs.lon  || $rootScope.DEFAULT_LONGITUDE;
            var lat  = attrs.lat  || $rootScope.DEFAULT_LATITUDE;

            var proj4326 = new OpenLayers.Projection("EPSG:4326");
            var projmerc = new OpenLayers.Projection("EPSG:900913");

            /*********************************/
            /* Utility methods               */
            /*********************************/

            // Return the messages associated with a cluster
            function getClusterMessages(feature) {
                var messages = [];
                for (var i = 0; i < feature.cluster.length; i++) {
                    var msi = feature.cluster[i].attributes.msi;
                    if ($.inArray(msi, messages) == -1 && !feature.cluster[i].attributes.bg) {
                        messages.push(msi);
                    }
                }
                return messages;
            }

            /*********************************/
            /* Layers                        */
            /*********************************/

            var msiContext = {
                strokeWidth: function(feature) {
                    return feature.attributes.bg ? 4 : 1.5;
                },
                strokeColor: function(feature) {
                    return feature.attributes.bg ? "#ffffff" : "#8f2f7b";
                },
                fillColor: function(feature) {
                    return feature.attributes.bg ? "#ffffff" : "#ad57a1";
                },
                fillOpacity: function(feature) {
                    return (feature.data.locType && (feature.data.locType == 'POLYGON' || feature.data.locType == 'CIRCLE')) ? 0.3 : 1.0;
                },
                graphicSize: function(feature) {
                    return 20;
                },
                graphicOffset: function(feature) {
                    return -msiContext.graphicSize() / 2;
                },
                description: function(feature) {
                    return feature.cluster ? getClusterMessages(feature).length + ' warnings' : feature.data.description;
                },
                icon: function(feature) {
                    return feature.cluster ? 'img/warn.png' : feature.data.icon;
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
                        fillOpacity: "${fillOpacity}",
                        pointRadius: 8,
                        strokeWidth: "${strokeWidth}",
                        strokeColor: "${strokeColor}",
                        strokeOpacity: 1.0,
                        label : "${description}",
                        fontFamily: "Courier New, monospace",
                        fontWeight: "bold",
                        fontSize: "11px",
                        fontColor: "#8f2f7b",
                        labelOutlineColor: "white",
                        labelOutlineWidth : 2,
                        labelYOffset: -20
                    }, { context: msiContext })
                }),
                strategies: [
                    new OpenLayers.Strategy.Cluster({
                        distance: 25,
                        threshold: 3
                    }),
                    new OpenLayers.Strategy.BBOX({
                        resFactor: 1,
                        update: function(options) {
                            if (options && this.getMapBounds() && scope.searchLocations) {
                                var b = this.getMapBounds().transform(projmerc, proj4326);
                                var locations = MapService.addBBoxToLocations(b);
                                scope.searchLocations({ locations: locations });
                            }
                        }
                    })
                ]
            });


            var url =  $location.protocol() + '://' + $location.host();
            if ($location.port() != 80 && $location.port() != 443) {
                url += ':' + $location.port();
            }
            var allMsiLayer = new OpenLayers.Layer.OSM("AllMsiLayer", url + "/msinm-tiles/${z}/${x}/${y}.png", {
                isBaseLayer: false,
                displayInLayerSwitcher: false,
                transitionEffect: null,
                visibility: false
            });

            // Build the array of layers to include
            var layers = [];
            // addBaseMapLayers() will add the base layers configured in conf/base-layers.js
            addBaseMapLayers(layers);
            // Add the mandatory layers
            layers.push(allMsiLayer);
            layers.push(msiLayer);

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
            /* Update MSI and NtM's          */
            /*********************************/

            // Crop the text to at most len characters
            function cropTxt(txt, len) {
                if (txt && txt.length > len) {
                    txt = txt.substring(0, len) + "\u2026";
                }
                return txt;
            }

            scope.$watch(attrs.searchResult, function (value) {

                // If the overflowed flag is set, display the MSI-NM background layer
                if (value) {
                    allMsiLayer.setVisibility((value.overflowed) ? true : false);
                }

                msiLayer.removeAllFeatures();
                if (value && value.messages)  {
                    var features = [];

                    for (var i in value.messages) {
                        var msg = value.messages[i];
                        var icon = "img/" + msg.seriesIdentifier.mainType.toLowerCase() + ".png";

                        for (var j in msg.locations) {
                            var loc = msg.locations[j];

                            var title = (msg.descs && msg.descs.length > 0) ? msg.descs[0].title : "N/A";

                            // Enable to display white outline of polygons
                            //var bgAttr = { id : i, description: cropTxt(title, 20), type : "msi", msi : msg, icon: icon, bg:true  };
                            //MapService.createLocationFeature(loc, bgAttr, features);

                            // Flick the "showVertices to true to show icons for each vertex
                            var attr = { id : i, description: cropTxt(title, 20), type : "msi", msi : msg, icon: icon, showVertices:false  };
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

            function onMsiSelect(event) {
                var messageId, messages;
                if (event.feature.cluster) {
                    // Cluster clicked
                    messages = getClusterMessages(event.feature);
                    messageId = messages[0].id;

                } else {
                    // Actual message clicked
                    messageId = event.feature.attributes.msi.id;
                    messages = scope.searchResult.messages;
                }

                $rootScope.$broadcast('messageDetails', {
                    messageId: messageId,
                    messages: messages
                });
                msiSelect.unselectAll();
            }

        }
    }
}]);


