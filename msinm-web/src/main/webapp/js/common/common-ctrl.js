/**
 * The home controller
 */
angular.module('msinm.common')
    .controller('FooterCtrl', ['$scope', '$modal',
        function ($scope, $modal) {
            'use strict';

            $scope.cookiesDlg = function () {
                $modal.open({
                    templateUrl: '../../partials/common/cookies.html',
                    size: 'lg'
                });
            };

            $scope.disclaimerDlg = function () {
                $modal.open({
                    templateUrl: '../../partials/common/disclaimer.html',
                    size: 'lg'
                });
            }
        }]);
