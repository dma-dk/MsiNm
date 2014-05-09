
/**
 * The main controller for the app.
 */
angular.module('msinm')
    .controller('MsiNmCtrl', ['$scope', '$routeParams', 'MsiNmService',
        function ($scope, $routeParams, MsiNmService) {
        'use strict';

        $scope.filterOnType = false;
        $scope.filterOnDate = false;
        $scope.filterOnLocation = false;

        $scope.query = '';
        $scope.status = 'ACTIVE';
        $scope.type = '';
        $scope.loc = '';
        $scope.dateFrom = '';
        $scope.dateTo = '';

        $scope.pageSize = 50;
        $scope.currentPage = 1;
        $scope.searchResult = { messages: [], startIndex: 0, total: 0 };

        $scope.sortBy = 'DATE';
        $scope.sortDesc = true;

        $scope.newSearch = function () {
            $scope.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            MsiNmService.search(
                $scope.query,
                $scope.status,
                $scope.type,
                $scope.loc,
                $("#messageDateFrom").val(),
                $("#messageDateTo").val(),
                $scope.pageSize,
                ($scope.currentPage - 1) * $scope.pageSize,
                $scope.sortBy,
                $scope.sortDesc ? 'DESC' : 'ASC',
                function(data) {
                    $scope.searchResult = data;
                    $scope.updateMap();
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
                    $scope.loc = '{"type":"CIRCLE", "radius":100, "points":[{"lat":' +
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

        $scope.showLocation = function () {
            $scope.filterOnLocation = true;
            if(!$scope.$$phase) {
                $scope.$apply();
            };
        };

        $scope.resetDate = function () {
            $scope.dateFrom = '';
            $scope.dateTo = '';
            $("#messageDateFrom").val('');
            $("#messageDateTo").val('');
        };

        $scope.pageChanged = function() {
            $scope.search();
        };

        $scope.toggleSortOrder = function() {
            $scope.sortDesc = !$scope.sortDesc;
            $scope.search();
        };

        $scope.updateMap = function() {
            locationLayer.removeAllFeatures();
            if ($scope.loc != '') {
                var features = [];
                var loc = JSON.parse($scope.loc);

                var attr = {
                    id : 1,
                    description: "location filter",
                    type : "loc"
                }

                createLocationFeature(loc, attr, features);
                locationLayer.addFeatures(features);
            }

            msiLayer.removeAllFeatures();
            var features = [];

            for (var i in $scope.searchResult.messages) {
                var msg = $scope.searchResult.messages[i];
                var loc = msg.messageItems[0].locations[0];

                var attr = {
                    id : i,
                    description: msg.messageItems[0].keySubject,
                    type : "msi",
                    msi : msg
                }

                createLocationFeature(loc, attr, features);

            }
            msiLayer.addFeatures(features);
        }
}]);