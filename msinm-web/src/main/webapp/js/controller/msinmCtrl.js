
/**
 * The main controller for the app.
 */
angular.module('msinm')
    .controller('MsiNmCtrl', ['$scope', '$routeParams', 'MsiNmService',
        function ($scope, $routeParams, MsiNmService) {
        'use strict';

        $scope.query = undefined;
        $scope.msinmList = [];
        $scope.filteredList = [];
        $scope.predicate = '-issueDate';

        $scope.loadMsiNm = function () {
            MsiNmService.list(function(data) {
                $scope.filteredList = $scope.msinmList = data;
                $scope.selection = '';
            },
            function () {
                alert("Error");
            });
        };


        $scope.onEdit = function () {
            $scope.filteredList = [];
            $.each($scope.msinmList, function(index, msi) {
               var q = $scope.query.toLowerCase();
               if (msi.messageItems[0].keySubject.toLowerCase().indexOf(q) != -1 ||
                   msi.generalArea.toLowerCase().indexOf(q) != -1 ||
                   msi.locality.toLowerCase().indexOf(q) != -1) {
                   $scope.filteredList.push(msi);
               }
            });
        };
    }]);