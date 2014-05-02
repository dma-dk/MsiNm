
/**
 * The main controller for the app.
 */
angular.module('msinm')
    .controller('MsiNmCtrl', ['$scope', '$routeParams', 'MsiNmService',
        function ($scope, $routeParams, MsiNmService) {
        'use strict';

        $scope.query = '';
        $scope.status = 'ACTIVE';
        $scope.type = '';
        $scope.loc = '';

        $scope.msinmList = [];

        $scope.search = function () {
            MsiNmService.search(
                $scope.query,
                $scope.status,
                $scope.type,
                $scope.loc,
                function(data) {
                    $scope.msinmList = data;
                },
                function () {
                    //alert("Error");
                }
            );
        };

        $scope.importMsiNm = function () {
            MsiNmService.importMsiNm(function(data) {
                    $scope.search();
                },
                function () {
                    //alert("Error");
                });
        };

        $scope.setCurrentLocation = function () {
            navigator.geolocation.getCurrentPosition(function(pos) {
                $scope.$apply(function() {
                    $scope.loc = '{"type":"CIRCLE", "radius":1, "points":[{"lat":' +
                        pos.coords.latitude + ',"lon":' + pos.coords.longitude + ',"num":1}]}';
                });
            });
        };

        $scope.resetType = function () {
            $scope.status = 'ACTIVE';
            $scope.type = '';
            $("#messageType").select2('data', null)
        };

        $scope.resetLocation = function () {
            $scope.loc = '';
        };
}]);