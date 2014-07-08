
/**
 * The main controller for the app.
 */
angular.module('msinm.search')
    .controller('SearchCtrl', ['$scope', '$window', '$location', '$modal', 'SearchService', '$http', 'growlNotifications',
        function ($scope, $window, $location, $modal, SearchService, $http, growlNotifications) {
        'use strict';

        $scope.filterOnType = false;
        $scope.filterOnDate = false;
        $scope.filterOnLocation = false;

        $scope.locationTool = 'navigation';

        $scope.query = '';
        $scope.status = 'ACTIVE';
        $scope.type = '';
        $scope.locations = [];
        $scope.dateFrom = '';
        $scope.dateTo = '';

        $scope.pageSize = 100;
        $scope.currentPage = 1;
        $scope.searchResult = { messages: [], startIndex: 0, total: 0 };

        $scope.sortBy = 'ID';
        $scope.sortDesc = false;

        $scope.showWms = false;

        $scope.viewMode = $location.path().endsWith("/map") ? "map" :
                ($location.path().endsWith("/table") ? "table" : "grid");

        $scope.newSearch = function () {
            $scope.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            SearchService.search(
                $scope.query,
                $scope.status,
                $scope.type,
                JSON.stringify($scope.locations),
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
                    growlNotifications.add('<h4>Search failed</h4>', 'danger', 3000);
                }
            );
        };

        $scope.resetType = function () {
            $scope.status = 'ACTIVE';
            $scope.type = '';
            $("#roles").select2('data', null);
        };

        $scope.resetLocation = function () {
            $scope.locations = [];
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

        $scope.toggleSortOrder = function(sortBy) {
            $scope.sortBy = sortBy;
            $scope.sortDesc = !$scope.sortDesc;
            $scope.search();
        };

        $scope.pdf = function () {
            $window.location = '/rest/message/pdf?lang=da'
            + '&q=' + encodeURIComponent($scope.query)
            + '&status=' + encodeURIComponent($scope.status)
            + '&type=' + encodeURIComponent($scope.type)
            + '&loc=' + encodeURIComponent(JSON.stringify($scope.locations))
            + '&from=' + encodeURIComponent($("#messageDateFrom").val())
            + '&to=' + encodeURIComponent($("#messageDateTo").val())
            + '&sortBy=' + ($scope.sortBy)
            + '&sortOrder=' + ($scope.sortDesc ? 'DESC' : 'ASC');
        };

        $scope.showLocationEditor = function(show) {
            if (show) {
                $("body").css("overflow", "hidden");
                //$('.location-editor').css('top', $(window).top() + 'px');
                //$('.location-editor').css('left', $(window).left() + 'px');
                //$('.location-editor').css('width', $(window).width() + 'px');
                //$('.location-editor').css('height', $(window).height() + 'px');
                $('.location-editor').fadeIn(0);
                $scope.locationsVisible = true;
            } else {
                $("body").css("overflow", "auto");
                $('.location-editor').fadeOut(0);
                $scope.locationsVisible = false;
            }
        }

}]);