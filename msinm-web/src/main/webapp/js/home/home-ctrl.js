/**
 * The home controller
 */
angular.module('msinm.common')
    .controller('HomeCtrl', ['$scope', '$routeParams', '$modal', 'SearchService',
        function ($scope, $routeParams, $modal, SearchService) {
            'use strict';

            $scope.searchResult = { messages: [], startIndex: 0, total: 0 };

            // Reset password parameters
            $scope.email = $routeParams.email;
            $scope.token = $routeParams.token;


            $scope.init = function () {
                // Update the list of active warnings
                SearchService.search(
                    '', // query
                    'ACTIVE',
                    '', // type
                    '[]', // location
                    '', // area
                    '', // from date
                    '', // to date
                    100,
                    0,
                    'DATE',
                    'DESC',
                    function (data) {
                        $scope.searchResult = data;
                    },
                    function () {
                        // Ignore errors
                    }
                );

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
            };
        }]);
