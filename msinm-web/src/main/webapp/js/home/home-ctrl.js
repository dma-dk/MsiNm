/**
 * The home controller
 */
angular.module('msinm.common')
    .controller('HomeCtrl', ['$scope', '$rootScope', '$routeParams', '$modal', '$timeout', 'MessageService', 'UserService',
        function ($scope, $rootScope, $routeParams, $modal, $timeout, MessageService, UserService) {
            'use strict';

            $scope.searchResult = { messages: [], startIndex: 0, total: 0 };

            $scope.firingExercisesToday = [];
            $scope.firingExercisesTomorrow = [];

            // Reset password parameters
            $scope.email = $routeParams.email;
            $scope.token = $routeParams.token;
            $scope.authToken = $routeParams.authToken;
            $scope.messageId = $routeParams.messageId;


            $scope.init = function () {

                if (!$scope.authToken) {
                    // Update the list of active warnings
                    MessageService.published(
                        'AREA',
                        'ASC',
                        function (data) {
                            $scope.searchResult = data;
                            $scope.checkGroupByArea(2);
                        },
                        function () {
                            // Ignore errors
                        }
                    );

                    // Update the list of active firing exercises
                    $timeout(function () {
                        MessageService.activeFiringExercises(
                            function (data) {
                                $scope.handleFiringExercises(data);
                            },
                            function () {
                                // Ignore errors
                            }
                        );
                    }, 100);
                }

                // Check if a reset password has been issued
                if ($scope.email && $scope.token) {
                    $modal.open({
                        controller: "NewPasswordCtrl",
                        templateUrl : "/partials/user/new-password.html",
                        size: 'sm',
                        resolve: {
                            email: function () {
                                return $scope.email;
                            },
                            token: function () {
                                return $scope.token;
                            }
                        }
                    });
                }

                // Check if this is an Auth token login
                if ($scope.authToken) {
                    UserService.authenticate(
                        $scope.authToken,
                        function(data) {
                            console.log("SUCCESS");
                            location.href = "/";
                        },
                        function(data, status) {
                            console.log("ERROR");
                            location.href = "/";
                        });

                }

                // Check if we should open a message details dialog
                if ($scope.messageId) {
                    $rootScope.$broadcast('messageDetails', {
                        messageId: $scope.messageId
                    });
                }

            };

            function timeInterval(msg) {
                return new Date(msg.validFrom).hhmm() + "-" + new Date(msg.validTo).hhmm()
            }

            // Update firing exercises lists
            $scope.handleFiringExercises = function(searchResult) {
                var today = new Date().toDateString();
                var tomorrow = new Date(new Date().getTime() + 24 * 60 * 60 * 1000).toDateString();
                for (var i in searchResult.messages) {
                    var msg = searchResult.messages[i];
                    var validFrom = new Date(msg.validFrom).toDateString();
                    var validTo = msg.validTo ? new Date(msg.validTo).toDateString() : undefined;
                    if (validTo && validFrom == today && validTo == today) {
                        $scope.firingExercisesToday.push(msg);
                        msg.timeInterval = timeInterval(msg);
                    } else if (validTo && validFrom == tomorrow && validTo == tomorrow) {
                        $scope.firingExercisesTomorrow.push(msg);
                        msg.timeInterval = timeInterval(msg);
                    }
                }
            };

            // Scans through the search result and marks all messages that should potentially display an area head line
            $scope.checkGroupByArea = function (maxLevels) {
                var lastAreaId = undefined;
                if ($scope.searchResult && $scope.searchResult.total > 0) {
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

        }]);
