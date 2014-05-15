/**
 * User-related controllers
 */

angular.module('msinm.user', [ ])
    .controller('UserCtrl', ['$scope', '$modal', 'MsiNmService',
        function ($scope, $modal, MsiNmService) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.user = { username:undefined, password: undefined };

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
            console.log("AUTH " + $scope.user.username);
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
        }
    }]);



