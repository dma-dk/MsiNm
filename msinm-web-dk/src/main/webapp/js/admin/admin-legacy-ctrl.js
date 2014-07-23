
/**
 * The main controllers for the admin app.
 */
angular.module('msinm.admin')

    .factory('LegacyService', [ '$http', function($http) {
        'use strict';

        return {

            getMsiImportType: function(success, error) {
                $http.get('/rest/import/legacy-msi/import-type')
                    .success(success)
                    .error(error);
            },

            setMsiImportType: function(type, success, error) {
                $http({
                    method: 'PUT',
                    url: '/rest/import/legacy-msi/import-type',
                    data: $.param({type: type}),
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                })
                    .success(success)
                    .error(error);
            }
        };
    }])


    /**
     * Legacy Controller
     */
    .controller('LegacyCtrl', ['$scope', '$location', '$modal', 'LegacyService',
        function ($scope, $location, $modal, LegacyService) {
            'use strict';

            $scope.legacyMsiImportType = 'NONE';
            $scope.legacyMsiImportTypes = [ 'NONE', 'ACTIVE', 'ALL' ];

            $scope.loadSettings = function() {
                LegacyService.getMsiImportType(function(data) {
                        $scope.legacyMsiImportType = data;
                    },
                    function () {
                        console.log("Error getting legacy MSI import type");
                    });
            };

            $scope.updateSettings = function() {
                console.log("Update settings ");
                LegacyService.setMsiImportType(
                    $scope.legacyMsiImportType,
                    function(data) {
                    },
                    function () {
                        console.log("Error setting legacy MSI import type");
                    });
            };

            // Open modal dialog for uploading and importing legacy NM PDF's.
            $scope.legacyNMImportDlg = function() {

                console.log("IMPORTING PDF");
                $scope.modalInstance = $modal.open({
                    templateUrl : "/partials/admin/legacy-nm-import.html",
                    controller: function ($scope) {
                        $scope.pdfFileUploaded = function(result) {
                            $scope.importResult = result;
                            if(!$scope.$$phase) {
                                $scope.$apply();
                            }
                        };
                    }
                });
            };


        }]);



