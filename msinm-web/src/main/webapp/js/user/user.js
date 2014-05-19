/**
 * User-related controllers
 */

angular.module('msinm.user', [ ])
    .controller('UserCtrl', ['$scope', '$modal', 'MsiNmService', 'Auth',
        function ($scope, $modal, MsiNmService, Auth) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.user = { email:undefined, password: undefined };

        $scope.loggedIn = false;

        $scope.loginDlg = function() {
            $scope.loginDialog = $modal.open({
                templateUrl : "/partials/login-dialog.html",
                resolve : {
                    msg : function() {
                        return typeof config === "object" ? config.msg : null;
                    }
                }
            });
            return $scope.loginDialog;
        };

        $scope.login = function() {

            MsiNmService.authenticate(
                $scope.user,
                function(data) {
                    console.log("SUCCESS");
                    $scope.$close();
                },
                function(data, status) {
                    console.log("ERROR");
                    $scope.user.password = undefined;
                    $scope.message = "Error in name or password";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

        $scope.logout = function() {
            Auth.logout();
        }
    }])

    .factory('Auth', ['$rootScope', '$window', function ($rootScope, $window) {
        'use strict';

        return {
            init: function() {
                if ($window.sessionStorage.jwt) {
                    $rootScope.currentUser = JSON.parse($window.sessionStorage.jwt);
                } else {
                    return undefined;
                }
            },

            login: function(jwtToken) {
                $window.sessionStorage.clear();
                $rootScope.currentUser = jwtToken;
                if (jwtToken) {
                    $window.sessionStorage.jwt = JSON.stringify(jwtToken);
                }
            },

            logout: function() {
                $window.sessionStorage.clear();
                $rootScope.currentUser = undefined;
            },

            isLoggedIn: function() {
                return ($rootScope.currentUser != undefined);
            },

            hasRole: function(role) {
                return $rootScope.currentUser && $rootScope.currentUser.indexOf(role) > -1;
            }

        }
    }])

    .run(['Auth', function (Auth) {
        Auth.init();
    }]);



