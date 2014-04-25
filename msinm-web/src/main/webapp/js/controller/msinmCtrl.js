
/**
 * The main controller for the app.
 */
angular.module('msinm')
    .controller('MsiNmCtrl', ['$scope', '$routeParams', 'MsiNmService',
        function ($scope, $routeParams, MsiNmService) {
        'use strict';

        $scope.msinmList = [];
        $scope.predicate = '-issueDate';

        $scope.loadMsiNm = function () {
            MsiNmService.list(function(data) {
                $scope.msinmList = data;
            },
            function () {
                alert("Error");
            });
        };

    }]);