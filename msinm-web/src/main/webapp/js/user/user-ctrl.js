/**
 * User-related controllers
 */

angular.module('msinm.user')
    .controller('UserCtrl', ['$scope', '$rootScope', '$cookieStore', 'UserService',
        function ($scope, $rootScope, $cookieStore, UserService) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.user = { email:$cookieStore.get('lastLogin'), password: undefined };


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
                    $scope.message = "Error in name or password";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

    }]);





