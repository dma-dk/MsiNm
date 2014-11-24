
/**
 * The main controllers for the legacy admin app.
 */
angular.module('msinm.admin')

    /**
     * Legacy Controller
     */
    .controller('LegacyCtrl', ['$scope',
        function ($scope) {
            'use strict';

            $scope.importResult = '';
            $scope.importData = { year: '' + new Date().getFullYear(), week: '' };


            $scope.xmlFileUploaded = function(result) {
                $scope.importResult = result;
                if(!$scope.$$phase) {
                    $scope.$apply();
                }
            };

    }]);

