
/**
 * The main controller for the app.
 */
angular.module('msinm')
    .controller('MsiNmCtrl', ['$scope', '$routeParams', 'MsiNmService',
        function ($scope, $routeParams, MsiNmService) {
        'use strict';

        $scope.query = '';
        $scope.msinmList = [];

        $scope.search = function () {
            MsiNmService.search(
                $scope.query,
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

}]);