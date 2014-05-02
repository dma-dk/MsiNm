
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
        $scope.msinmList = [];

        $scope.search = function () {
            MsiNmService.search(
                $scope.query,
                $scope.status,
                $scope.type,
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