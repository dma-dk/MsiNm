/**
 * User-related controllers
 */

angular.module('msinm.user')
    .controller('UserCtrl', ['$scope', '$modal', '$cookieStore', 'UserService', 'Auth',
        function ($scope, $modal, $cookieStore, UserService, Auth) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.user = { email:$cookieStore.get('lastLogin'), password: undefined };

        $scope.$on('Login', function (event, message) {
            $scope.loginDlg();
            $scope.message = message;
        });

        $scope.loginDlg = function() {
            $scope.loginDialog = $modal.open({
                templateUrl : "/partials/login-dialog.html"
            });
            return $scope.loginDialog;
        };

        $scope.login = function() {

            UserService.authenticate(
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




