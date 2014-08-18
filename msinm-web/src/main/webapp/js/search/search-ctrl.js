
/**
 * The message search controller for the app.
 */
angular.module('msinm.search')
    .controller('SearchCtrl', ['$scope', '$window', '$location', '$modal', 'MessageService', 'DialogService', '$http', 'growlNotifications',
        function ($scope, $window, $location, $modal, MessageService, DialogService, $http, growlNotifications) {
        'use strict';

        $scope.focusMe = true;
        $scope.dateFormat = "dd-mm-yyyy";
        $scope.today = new Date().formatDate($scope.dateFormat);

        $scope.filterOnType = false;
        $scope.filterOnDate = false;
        $scope.filterOnLocation = false;

        $scope.query = '';
        $scope.status = 'PUBLISHED';
        $scope.type = '';
        $scope.locations = [];
        $scope.areas = '';
        $scope.dateFrom = '';
        $scope.dateTo = '';

        $scope.pageSize = 100;
        $scope.currentPage = 1;
        $scope.searchResult = { messages: [], startIndex: 0, total: 0 };
        $scope.paginationVisible = false;

        $scope.sortBy = 'ID';
        $scope.sortDesc = true;

        $scope.mapMode = false;
        $scope.bbox = [];

        // Remove the style="display: none" attribute. A trick
        // used to avoid the panel being visible when reloading the page.
        $(".searchFilterPanel").removeAttr('style');

        $scope.newSearch = function () {
            $scope.currentPage = 1;
            $scope.search();
        };

        $scope.searchLocations = function(locations) {
            $scope.bbox = locations;
            $scope.search();
            if(!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.search = function () {
            MessageService.search(
                $scope.query,
                $scope.status,
                $scope.type,
                JSON.stringify($scope.mapMode ? $scope.bbox : $scope.locations),
                $scope.areas,
                $("#dateFrom").val(),
                $("#dateTo").val(),
                $scope.pageSize,
                ($scope.currentPage - 1) * $scope.pageSize,
                $scope.sortBy,
                $scope.sortDesc ? 'DESC' : 'ASC',
                $scope.mapMode,
                function(data) {
                    $scope.searchResult = data;
                    $scope.paginationVisible = (data && data.total > $scope.pageSize);
                    if (data && data.overflowed) {
                        growlNotifications.add('<strong>Search result too big</strong><br>Zoom in or filter search', 'info', 1500);
                    }
                },
                function () {
                    growlNotifications.add('<h4>Search failed</h4>', 'danger', 3000);
                }
            );
        };

        $scope.$watch(function () {
            return $location.path();
        }, function (newValue, oldValue) {

            // CSS update
            if (newValue.endsWith("/map")) {
                $(".searchFilterPanel").addClass("mapSearchFilterPanel");
            } else if (oldValue.endsWith("/map")) {
                $(".searchFilterPanel").removeClass("mapSearchFilterPanel");
            }

            // Switching between view modes
            var wasMapMode = $scope.mapMode;
            if (newValue.endsWith("/map")) {
                $scope.mapMode = true;
                $scope.pageSize = 10000;
                $scope.currentPage = 1;
            } else {
                $scope.mapMode = false;
                $scope.pageSize = 100;
            }

            // Called initially, and when entering and leaving the map view
            if (!$scope.mapMode) {
                $scope.searchResult = { messages: [], startIndex: 0, total: 0 };
                $scope.newSearch();
            }
        });

        $scope.toggleFilterOnType = function () {
            $scope.filterOnType = !$scope.filterOnType;
            if (!$scope.filterOnType) {
                $scope.status = 'PUBLISHED';
                $scope.type = '';
                $("#messageType").select2('data', null);
            }
        };

        $scope.toggleFilterOnLocation = function () {
            $scope.filterOnLocation = !$scope.filterOnLocation;
            if (!$scope.filterOnLocation) {
                $scope.locations = [];
                $scope.areas = '';
                $("#messageArea").select2('data', null);
            }
        };

        $scope.resetLocation = function () {
            $scope.locations = [];
        };

        $scope.toggleFilterOnDate = function () {
            $scope.filterOnDate = !$scope.filterOnDate;
            if (!$scope.filterOnDate) {
                $scope.dateFrom = '';
                $scope.dateTo = '';
                $("#dateFrom").val('');
                $("#dateTo").val('');
            }
        };

        $scope.pageChanged = function() {
            $scope.search();
        };

        $scope.toggleSortOrder = function(sortBy) {
            if (sortBy == $scope.sortBy) {
                $scope.sortDesc = !$scope.sortDesc;
            } else {
                $scope.sortBy = sortBy;
            }
            $scope.search();
        };

        $scope.sortIndicator = function(sortBy) {
            if (sortBy == $scope.sortBy) {
                return $scope.sortDesc ? '&#9650;' : '&#9660';
            }
            return "";
        };

        $scope.pdf = function () {
            $window.location = '/rest/messages/search-pdf?'
            + 'lang=' + $scope.language
            + '&q=' + encodeURIComponent($scope.query)
            + '&status=' + encodeURIComponent($scope.status)
            + '&type=' + encodeURIComponent($scope.type)
            + '&loc=' + encodeURIComponent(JSON.stringify($scope.mapMode ? $scope.bbox : $scope.locations))
            + '&areas=' + encodeURIComponent($scope.areas)
            + '&from=' + encodeURIComponent($("#dateFrom").val())
            + '&to=' + encodeURIComponent($("#dateTo").val())
            + '&sortBy=' + ($scope.sortBy)
            + '&sortOrder=' + ($scope.sortDesc ? 'DESC' : 'ASC');
        };

        $scope.calendar = function()  {

            var calUrl =  $location.protocol() + '://' + $location.host();
            if ($location.port() != 80 && $location.port() != 443) {
                calUrl += ':' + $location.port();
            }
            calUrl += '/rest/messages/active-msinm.ics?lang=' + $scope.language;

            var modalOptions = {
                closeButtonText: 'OK',
                actionButtonText: 'Dowload',
                headerText: 'Subscribe to Calendar',
                calUrl: calUrl,
                templateUrl: "addToCalendar.html"
            };

            DialogService.showDialog({}, modalOptions).then(function (result) {
                $window.location = calUrl;
            });
        };

        $scope.showLocationEditor = function(show) {
            if (show) {
                $("body").css("overflow", "hidden");
                $('.location-editor').fadeIn(0);
                $scope.locationsVisible = true;
            } else {
                $("body").css("overflow", "auto");
                $('.location-editor').fadeOut(0);
                $scope.locationsVisible = false;
            }
        }

}]);