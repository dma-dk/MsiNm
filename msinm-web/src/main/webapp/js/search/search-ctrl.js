
/**
 * The message search controller for the app.
 */
angular.module('msinm.search')
    .controller('SearchCtrl', ['$scope', '$window', '$location', '$modal', 'MessageService', 'DialogService', 'LangService', '$http', 'growlNotifications',
        function ($scope, $window, $location, $modal, MessageService, DialogService, LangService, $http, growlNotifications) {
        'use strict';

        $scope.action = "search";
        $scope.firstSearch = true;

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
        $scope.categories = '';
        $scope.charts = '';
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

        // Assemple the current search params
        $scope.params = function (paged) {
            var p = 'lang=' + $scope.language
                + '&q=' + encodeURIComponent($scope.query)
                + '&status=' + encodeURIComponent($scope.status)
                + '&type=' + encodeURIComponent($scope.type)
                + '&loc=' + encodeURIComponent(JSON.stringify($scope.mapMode ? $scope.bbox : $scope.locations))
                + '&areas=' + encodeURIComponent($scope.areas)
                + '&categories=' + encodeURIComponent($scope.categories)
                + '&charts=' + encodeURIComponent($scope.charts)
                + '&from=' + encodeURIComponent($("#dateFrom").val())
                + '&to=' + encodeURIComponent($("#dateTo").val())
                + '&sortBy=' + ($scope.sortBy)
                + '&sortOrder=' + ($scope.sortDesc ? 'DESC' : 'ASC');

            if (paged) {
                p = p + '&maxHits=' + $scope.pageSize
                    + '&startIndex=' + ($scope.currentPage - 1) * $scope.pageSize
                    + '&mapMode=' + $scope.mapMode;
            }

            return p;
        }

        $scope.search = function () {
            MessageService.search(
                $scope.params(true),
                function(data) {
                    $scope.searchResult = data;
                    $scope.paginationVisible = (data && data.total > $scope.pageSize);
                    if (data && data.overflowed) {
                        growlNotifications.add('<strong>Search result too big</strong><br>Zoom in or filter search', 'info', 1500);
                    }
                    $scope.checkGroupByArea(2);
                },
                function () {
                    growlNotifications.add('<h4>Search failed</h4>', 'danger', 3000);
                }
            );
        };


        // Scans through the search result and marks all messages that should potentially display an area head line
        $scope.checkGroupByArea = function (maxLevels) {
            var lastAreaId = undefined;
            if ($scope.searchResult && $scope.searchResult.total > 0 && $scope.sortBy == 'AREA') {
                for (var m in $scope.searchResult.messages) {
                    var msg = $scope.searchResult.messages[m];
                    var areas = [];
                    for (var area = msg.area; area !== undefined; area = area.parent) {
                        areas.unshift(area);
                    }
                    if (areas.length > 0) {
                        area = areas[Math.min(areas.length - 1, maxLevels - 1)];
                        if (!lastAreaId || area.id != lastAreaId) {
                            lastAreaId = area.id;
                            msg.areaHeading = area;
                        }
                    }
                }
            }
        };

        // Watch the location path to handle various search result view modes and edit mode.
        $scope.$watch(function () {
            return $location.path();
        }, function (newValue, oldValue) {

            // Determine if this is a search page or the edit page
            if (newValue.indexOf("/search/edit/") == 0) {
                $scope.action = "edit";
            } else {
                $scope.action = "search";
            }

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
            } else if ($scope.action == 'search') {
                $scope.mapMode = false;
                $scope.pageSize = 100;
            }

            var wasFirstSearch = $scope.firstSearch;
            if ($scope.action == "search" && wasFirstSearch) {
                $scope.firstSearch = false;
            }

            // Called initially, and when entering and leaving the map view
            if ($scope.action == 'search' && !$scope.mapMode && (wasMapMode || wasFirstSearch)) {
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
                $scope.categories = '';
                $("#messageCategories").select2('data', null);
            }
        };

        $scope.toggleFilterOnLocation = function () {
            $scope.filterOnLocation = !$scope.filterOnLocation;
            if (!$scope.filterOnLocation) {
                $scope.locations = [];
                $scope.areas = '';
                $("#messageArea").select2('data', null);
                $scope.charts = '';
                $("#messageCharts").select2('data', null);
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
            $window.location = '/rest/messages/search-pdf?' + $scope.params(false);
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

        $scope.showLocationEditor = function() {
            $scope.modalInstance = $modal.open({
                controller: "SearchLocationCtrl",
                templateUrl : "locationSelector.html",
                size: 'lg',
                resolve: {
                    locations: function(){
                        return $scope.locations;
                    }
                }
            });
        };

        $scope.createMailingList = function () {
            $modal.open({
                controller: "NewMailingListCtrl",
                templateUrl : "partials/user/new-mailing-list-dialog.html",
                resolve: {
                    filterParams: function(){
                        return $scope.params(false);
                    }
                }
            });
        }

    }])

    /**
     * Controller for search location selection
     */
    .controller('SearchLocationCtrl', ['$scope', '$timeout', 'locations',
        function ($scope, $timeout, locations) {
            'use strict';

            $scope.locations = locations;

            // Get OpenLayers to refresh it's size
            $scope.init = function() {
                $timeout(function() {
                    $scope.visible = true;
                }, 100);
            }
        }]);
