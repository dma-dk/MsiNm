/**
 * User-related controllers
 */

angular.module('msinm.user')

    /**
     * The UserCtrl handles login and registration of new users
     */
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
                    $scope.error = undefined;
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
            $modal.open({
                controller: "UserCtrl",
                templateUrl : "/partials/user/registration-dialog.html"
            });
        };

        $scope.register = function() {
            UserService.registerUser(
                $scope.newUser.email,
                $scope.newUser.firstName,
                $scope.newUser.lastName,
                $scope.newUser.language,
                $scope.newUser.mmsi,
                $scope.newUser.vesselName,
                function(data) {
                    console.log("User " + $scope.newUser.email + " registered");
                    $scope.viewMode = "info";
                    $scope.error = undefined;
                    $scope.message = $scope.newUser.email + " has been registered as a new user. An activation email has been sent to you.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function(data) {
                    $scope.error = "An error happened: " + data + ".";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        }


    }])

    /**
     * The NewPasswordCtrl handles setting a new password
     */
    .controller('NewPasswordCtrl', ['$scope', '$location', 'UserService', 'email', 'token',
        function ($scope, $location, UserService, email, token) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = { email:email, password: undefined, password2: undefined };
        $scope.token = token;
        $scope.viewMode = "new-pwd";

        $scope.$on("$destroy", function() {
            $location.path("/");
        });

        $scope.updatePassword = function() {
            UserService.updatePassword(
                $scope.user.email,
                $scope.user.password,
                $scope.token,
                function(data) {
                    $scope.error = undefined;
                    $scope.message = "The password has been updated.";
                    $scope.viewMode = "info";
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
        }

    }])


    /**
     * The NewPasswordCtrl handles adding or editing a user by an administrator
     */
    .controller('AddOrEditUserCtrl', ['$scope', '$modalInstance', 'UserService', 'user', 'userAction',
        function ($scope, $modalInstance, UserService, user, userAction) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = user;
        $scope.roles = {
            user: $.inArray('user', user.roles) > -1,
            admin: $.inArray('admin', user.roles) > -1
        };
        $scope.userAction = userAction;

        $scope.createOrUpdateUser = function() {
            var roles = [];
            if ($scope.roles.user) {
                roles.push("user");
            }
            if ($scope.roles.admin) {
                roles.push("admin");
            }
            UserService.createOrUpdateUser(
                $scope.user.email,
                $scope.user.firstName,
                $scope.user.lastName,
                $scope.user.language,
                roles,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the email address.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        }

    }]);




