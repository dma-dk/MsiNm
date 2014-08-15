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
                    MessageService.search(
                        '', // query
                        'PUBLISHED',
                        '', // type
                        '[]', // location
                        '', // area
                        '', // from date
                        '', // to date
                        100,
                        0,
                        'DATE',
                        'DESC',
                        false,
                        function (data) {
                            $scope.searchResult = data;
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
        }]);
