/**
 * User-related controllers
 */

angular.module('msinm.user')
    .controller('UserCtrl', ['$scope', '$rootScope', '$cookieStore', '$modal', 'UserService',
        function ($scope, $rootScope, $cookieStore, $modal, UserService) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = { email:$cookieStore.get('lastLogin'), password: undefined };

        $scope.newUser = {};

        $scope.viewMode = "login";

        $scope.$on("$destroy", function() {
            delete $rootScope.loginDialog;
        });

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
                    $scope.error = "Error in name or password";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

        $scope.setViewMode = function (viewMode) {
            $scope.viewMode = viewMode;
        };

        $scope.resetPassword = function () {
            $scope.message = "Resetting password...";
            $scope.viewMode = "info";

            UserService.resetPassword(
                $scope.user.email,
                function(data) {
                    $scope.message = "An email has been sent to " + $scope.user.email
                        + ". Please follow the instructions to reset your password.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the email address.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

        $scope.registerDlg = function() {
            $scope.newUser = {  email: '', firstName: '', lastName: '', password: undefined, pasasword2: undefined };
            $modal.open({
                controller: "UserCtrl",
                templateUrl : "/partials/user/registration-dialog.html"
            });
        };

        $scope.register = function() {
            alert("Register user " + $scope.newUser.email);
        }


    }])

    .controller('NewPasswordCtrl', ['$scope', '$modalInstance', '$location', 'UserService', 'email', 'token',
        function ($scope, $modalInstance, $location, UserService, email, token) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = { email:email, password: undefined, password2: undefined };
        $scope.token = token;

        $scope.$on("$destroy", function() {
            $location.path("/");
        });

        $scope.updatePassword = function() {
            UserService.updatePassword(
                $scope.user.email,
                $scope.user.password,
                $scope.token,
                function(data) {
                    $modalInstance.close("Closed");
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the email address.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        }

    }]);





