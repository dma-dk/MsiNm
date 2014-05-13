
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

            var map = new OpenLayers.Map({
                div: element[0],
                theme: null,
                layers: [
                    new OpenLayers.Layer.OSM("OpenStreetMap"),
                    msiLayer,
                    locLayer
                ],
                units : "degrees",
                projection : projmerc,
                center: new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc),
                zoom: zoom
            });


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

        }
    }
}]);


