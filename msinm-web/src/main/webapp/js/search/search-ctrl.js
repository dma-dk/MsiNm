
/**
 * The message search controller for the app.
 */
angular.module('msinm.search')
    .controller('SearchCtrl', ['$scope', '$window', '$location', '$modal', 'MessageService', 'DialogService', '$http', 'growlNotifications',
        function ($scope, $window, $location, $modal, MessageService, DialogService, $http, growlNotifications) {
        'use strict';

        $scope.focusMe = true;

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

        // Remove the style="display: none" attribute. A trick
        // used to avoid the panel being visible when reloading the page.
        $(".searchFilterPanel").removeAttr('style');

        $scope.newSearch = function () {
            $scope.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            MessageService.search(
                $scope.query,
                $scope.status,
                $scope.type,
                JSON.stringify($scope.locations),
                $scope.areas,
                $("#messageDateFrom").val(),
                $("#messageDateTo").val(),
                $scope.pageSize,
                ($scope.currentPage - 1) * $scope.pageSize,
                $scope.sortBy,
                $scope.sortDesc ? 'DESC' : 'ASC',
                function(data) {
                    $scope.searchResult = data;
                    $scope.paginationVisible = (data && data.total > $scope.pageSize);
                },
                function () {
                    growlNotifications.add('<h4>Search failed</h4>', 'danger', 3000);
                }
            );
        };

        $scope.$watch(function () {
            return $location.path();
        }, function (newValue, oldValue) {
            if (newValue.endsWith("/map")) {
                $(".searchFilterPanel").addClass("box-shadow-small");
            } else if (oldValue.endsWith("/map")) {
                $(".searchFilterPanel").removeClass("box-shadow-small");
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
                $("#messageDateFrom").val('');
                $("#messageDateTo").val('');
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
        }

        $scope.pdf = function () {
            $window.location = '/rest/messages/search-pdf?'
            + 'lang=' + $scope.language
            + '&q=' + encodeURIComponent($scope.query)
            + '&status=' + encodeURIComponent($scope.status)
            + '&type=' + encodeURIComponent($scope.type)
            + '&loc=' + encodeURIComponent(JSON.stringify($scope.locations))
            + '&areas=' + encodeURIComponent($scope.areas)
            + '&from=' + encodeURIComponent($("#messageDateFrom").val())
            + '&to=' + encodeURIComponent($("#messageDateTo").val())
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