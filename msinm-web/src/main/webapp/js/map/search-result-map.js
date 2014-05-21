
/**
 * Converts a div into a search result map
 */
angular.module('msinm.map')
    .directive('msiSearchResultMap', ['MapService', function (MapService) {
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
            var wmsLayer = new OpenLayers.Layer.WMS("WMS", "/wms/", {
                layers : 'cells',
                servicename : 'soe_enc',
                transparent : 'true',
                styles : 'default',
                login : 'StatSofart',
                password : '114karls'
            }, {

                isBaseLayer : false,
                visibility : false,
                projection : 'EPSG:3857'
                // ,getURL:get_my_url
            });

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

            var msiLayer  = new OpenLayers.Layer.Vector( "Msi-Nm", {
                styleMap: new OpenLayers.StyleMap({
                    "default": new OpenLayers.Style({
                        fillColor: "#ad57a1",
                        fillOpacity: 0.5,
                        pointRadius: 8,
                        strokeWidth: 2,
                        strokeColor: "#8f2f7b",
                        strokeOpacity: 0.8
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
                    wmsLayer,
                    msiLayer,
                    locLayer
                ],
                units : "degrees",
                projection : projmerc,
                center: new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc),
                zoom: zoom
            });

            /*********************************/
            /* Update the location feature   */
            /*********************************/
            scope.$watch(attrs.loc, function (value) {

                locLayer.removeAllFeatures();
                if (value && value != '') {
                    var features = [];
                    try {
                        var attr = {
                            id: 1,
                            description: "location filter",
                            type: "loc"
                        }

                        MapService.createLocationFeature(value, attr, features);
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
                        var loc = msg.messageItems[0].locations[0];

                        var attr = {
                            id : i,
                            description: msg.messageItems[0].keySubject,
                            type : "msi",
                            msi : msg
                        }

                        MapService.createLocationFeature(loc, attr, features);

                    }
                    msiLayer.addFeatures(features);
                }
            });

            /*********************************/
            /* Update the location feature   */
            /*********************************/
            scope.$watch(attrs.showWms, function (value) {
                wmsLayer.setVisibility(value);
            });

            /*********************************/
            /* Pop-ups for the features      */
            /*********************************/
            var msiSelect = new OpenLayers.Control.SelectFeature(msiLayer);
            msiLayer.events.on({
               "featureselected": onMsiSelect,
               "featureunselected": onMsiUnselect
            });
            map.addControl(msiSelect);
            msiSelect.activate();

            function onPopupClose(evt) {
                msiSelect.unselectAll();
            }

            function onMsiSelect(event) {
                var popup = new OpenLayers.Popup.FramedCloud("msi",
                    event.feature.geometry.getBounds().getCenterLonLat(),
                    new OpenLayers.Size(100,100),
                    '<p>' + event.feature.attributes.description + '</p>',
                    null,
                    true,
                    onPopupClose);
                event.feature.popup = popup;
                map.addPopup(popup);
            }

            function onMsiUnselect(event) {
                if(event.feature.popup) {
                    map.removePopup(event.feature.popup);
                    event.feature.popup.destroy();
                    delete event.feature.popup;
                }
            }

            /*********************************/
            /* Handle WMS layer events       */
            /*********************************/
/*
            map.events.register('zoomend', map, function(event) {
                var map = event.object;
                wmsLayer.setVisibility(map.getZoom() > 8);
                wmsLayer.setVisibility(map.getZoom() < 9);
            });
*/
        }
    }
}]);


