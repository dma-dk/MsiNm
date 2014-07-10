
/**
 * The main controllers for the admin app.
 */
angular.module('msinm.admin')

    .factory('LegacyService', [ '$http', function($http) {
        'use strict';

        return {

            importMsiNm: function(success, error) {
                $http.get('/rest/import/legacy-ws-msi')
                    .success(success)
                    .error(error);
            },

            importLegacyMsi: function(count, success, error) {
                $http.get('/rest/import/legacy-db-msi?limit=' + count)
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

            $scope.importCount = 500;

            $scope.importActiveMsi = function () {
                LegacyService.importMsiNm(function(data) {
                        console.log("Imported legacy MSI");
                    },
                    function () {
                        console.log("Error importing legacy MSI");
                    });
            };

            $scope.legacyMsiImportDlg = function() {

                $scope.importLegacyMsiDialog = $modal.open({
                    templateUrl : "/partials/admin/import-legacy-msi-dialog.html"
                });
                return $scope.importLegacyMsiDialog;
            };

            $scope.legacyMsiImport = function() {
                LegacyService.importLegacyMsi(
                    $scope.importCount,
                    function(data) {
                        console.log("Imported legacy DB MSI");
                    },
                    function () {
                        console.log("Error importing legacy DB MSI");
                    });

                $scope.$close();
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



