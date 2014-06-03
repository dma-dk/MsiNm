
/**
 * The main controllers for the admin app.
 */
angular.module('msinm.admin')
    .controller('LegacyCtrl', ['$scope', '$location', '$modal', 'LegacyService',
        function ($scope, $location, $modal, LegacyService) {
        'use strict';

        $scope.importCount = 500;

        $scope.importMsiNm = function () {
            LegacyService.importMsiNm(function(data) {
                    console.log("Imported legacy MSI");
                },
                function () {
                    console.log("Error importing legacy MSI");
                });
        };

        $scope.legacyImportDlg = function() {

            $scope.importLegacyMsiDialog = $modal.open({
                templateUrl : "/partials/admin/import-legacy-msi-dialog.html"
            });
            return $scope.importLegacyMsiDialog;
        };

        $scope.legacyImport = function() {
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

        $scope.recreateSearchIndex = function () {
            LegacyService.recreateSearchIndex(
                function(data) {
                    console.log("Initiated a rebuild of message search index");
                },
                function () {
                    console.log("Error initiating a rebuild of message search index");
                });
        };


        }]);
