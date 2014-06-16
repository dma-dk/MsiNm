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
        }])

    /**
     * File upload controller, based on:
     * https://github.com/nervgh/angular-file-upload
     */
    .controller('UploadCtrlXXX', ['$scope', '$fileUploader', 'Auth',
        function ($scope, $fileUploader, Auth) {
            'use strict';

            // create a uploader with options
            var uploader = $scope.uploader = $fileUploader.create({
                scope: $scope,
                url: '/rest/repo/upload'
            });

            // Handle authenticaiton
            if (Auth.isLoggedIn()) {
                uploader.headers.Authorization = Auth.authorizationHeader();
            }

            $scope.cancelOrRemove = function(item) {
                if (item.isUploading) {
                    item.cancel();
                } else {
                    item.remove();
                }
            }

        }]);
