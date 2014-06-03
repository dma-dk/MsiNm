
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
    }])

    .controller('UserCtrl', ['$scope', '$location', '$modal', 'UserService',
        function ($scope, $location, $modal, UserService) {
        'use strict';

        $scope.users = [];

        $scope.listUsers = function () {
            UserService.listUsers(
                function(data) {
                    $scope.users = data;
                },
                function () {
                    console.log("Error loading users");
                });
        };

    }])

    .controller('OperationsCtrl', ['$scope', '$location', '$modal', 'OperationsService',
        function ($scope, $location, $modal, OperationsService) {
        'use strict';

        $scope.recreateSearchIndex = function () {
            OperationsService.recreateSearchIndex(
                function(data) {
                    console.log("Initiated a rebuild of message search index");
                },
                function () {
                    console.log("Error initiating a rebuild of message search index");
                });
        };

        $scope.clearCaches = function () {
            OperationsService.clearCaches(
                "all",
                function(data) {
                    console.log("Clear all caches");
                },
                function () {
                    console.log("Error clearing all caches");
                });
        };

    }]);
