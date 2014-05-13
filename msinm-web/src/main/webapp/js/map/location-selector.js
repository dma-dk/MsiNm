/**
 * Defines a list of tools to select a location either as a point,
 * a circle, a polygon, a box or a line.
 */
angular.module('msinm.map')
    .directive('msiLocationSelector', ['MapService', function (MapService) {
        'use strict';

    return {
        restrict: 'AE',

        templateUrl: '/js/map/location-selector.html',

        scope: {
            tool: '=tool',
            loc: '=loc'
        },

        link: function (scope, element, attrs) {

            scope.newPt = { lat: undefined, lon: undefined };

            scope.deletePoint = function(pt) {
                scope.loc.points.splice(scope.loc.points.indexOf(pt), 1);
            }

            scope.addPoint = function() {
                scope.loc.points.push(scope.newPt);
                scope.newPt = { lat: undefined, lon: undefined };
            }

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


        }
    }
}]);
