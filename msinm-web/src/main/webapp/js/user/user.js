/**
 * User-related controllers
 */

angular.module('msinm.user', [ ])
    .controller('UserCtrl', ['$scope', '$modal', '$cookieStore', 'MsiNmService', 'Auth',
        function ($scope, $modal, $cookieStore, MsiNmService, Auth) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.user = { email:$cookieStore.get('lastLogin'), password: undefined };

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
                    $cookieStore.put('lastLogin', $scope.user.email);
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

        var storage =  $window.sessionStorage;
        //var storage =  $window.localStorage;

        return {
            init: function() {
                if (storage.jwt) {
                    $rootScope.currentUser = JSON.parse(storage.jwt);
                } else {
                    return undefined;
                }
            },

            login: function(jwtToken) {
                storage.clear();
                $rootScope.currentUser = jwtToken;
                if (jwtToken) {
                    storage.jwt = JSON.stringify(jwtToken);
                }
            },

            logout: function() {
                storage.clear();
                delete $rootScope.currentUser;
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



