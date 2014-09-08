/**
 * The home controller
 */
angular.module('msinm.common')
    .controller('HomeCtrl', ['$scope', '$rootScope', '$routeParams', '$modal', 'MessageService', 'UserService',
        function ($scope, $rootScope, $routeParams, $modal, MessageService, UserService) {
            'use strict';

            $scope.searchResult = { messages: [], startIndex: 0, total: 0 };

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
