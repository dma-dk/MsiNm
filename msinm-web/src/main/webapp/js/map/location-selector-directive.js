/**
 * Defines a list of tools to select a location either as a point,
 * a circle, a polygon, a box or a line.
 */
angular.module('msinm.map')
    .directive('msiLocationSelector', ['MapService', function (MapService) {
        'use strict';

    return {
        restrict: 'AE',

        templateUrl: '/partials/location-selector.html',

        scope: {
            tool: '=tool',
            loc: '=loc'
        },

        link: function (scope, element, attrs) {

            scope.newPt = { lat: undefined, lon: undefined };

            scope.deletePoint = function(pt) {
                scope.loc.points.splice(scope.loc.points.indexOf(pt), 1);
            };

            scope.addPoint = function() {
                scope.newPt.index = scope.loc.points.length + 1;
                scope.loc.points.push(scope.newPt);
                scope.newPt = { lat: undefined, lon: undefined };
            };

            scope.setCurrentLocation = function () {
                navigator.geolocation.getCurrentPosition(function(pos) {
                    scope.loc = {
                        type:"CIRCLE",
                        radius:100,
                        points:[{lat: pos.coords.latitude, lon: pos.coords.longitude, num: 1 }]
                    };
                    if(!scope.$$phase) {
                        scope.$apply();
                    }
                });
            };

            scope.$watch(function() {
                return scope.tool;
            }, function (value) {
                if (value && value != 'navigation' && value != scope.loc.type) {
                    if (value == 'point') {
                        scope.loc = {
                            type:"POINT",
                            points:[ { lat:undefined, lon:undefined, index: 1} ]
                        };
                    } else if (value == 'circle') {
                        scope.loc = {
                            type:"CIRCLE",
                            radius:100,
                            points:[ { lat:undefined, lon:undefined, index: 1} ]
                        };
                    } else if (value == 'polygon' || value == 'polyline') {
                        scope.loc = {
                            type: (value == 'polygon') ? "POLYGON" : "POLYLINE",
                            points:[  ]
                        };
                    }
                }
            }, true);
        }
    }
}]);
