
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

        $scope.locationTool = 'navigation';

        $scope.query = '';
        $scope.status = 'ACTIVE';
        $scope.type = '';
        $scope.loc = {};
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
                $scope.loc.type ? JSON.stringify($scope.loc) : '',
                $("#messageDateFrom").val(),
                $("#messageDateTo").val(),
                $scope.pageSize,
                ($scope.currentPage - 1) * $scope.pageSize,
                $scope.sortBy,
                $scope.sortDesc ? 'DESC' : 'ASC',
                function(data) {
                    $scope.searchResult = data;
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

        $scope.resetType = function () {
            $scope.status = 'ACTIVE';
            $scope.type = '';
            $("#messageType").select2('data', null)
        };

        $scope.resetLocation = function () {
            $scope.loc = {};
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

}]);