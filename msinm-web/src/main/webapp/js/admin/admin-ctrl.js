
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

    .controller('AdminUserCtrl', ['$scope', '$location', '$modal', 'AdminUserService',
        function ($scope, $location, $modal, AdminUserService) {
        'use strict';

        $scope.users = [];
        $scope.search = '';
        $scope.user = undefined;
        $scope.userAction = undefined;

        $scope.listUsers = function () {
            AdminUserService.listUsers(
                function(data) {
                    $scope.users = data;
                },
                function () {
                    console.log("Error loading users");
                });
        };

        $scope.addUser = function () {
            $scope.userAction = 'add';
            $scope.user = { email:undefined, firstName: undefined, lastName: undefined, roles: ['user'] };
            $scope.userDlg();
        };

        $scope.editUser = function (user) {
            $scope.userAction = 'edit';
            $scope.user = angular.copy(user);
            $scope.userDlg();
        };

        $scope.userDlg = function () {
            $scope.modalInstance = $modal.open({
                controller: "AddOrEditUserCtrl",
                templateUrl : "/partials/user/add-edit-dialog.html",
                resolve: {
                    user: function(){
                        return $scope.user;
                    },
                    userAction: function(){
                        return $scope.userAction;
                    }
                }
            });

            $scope.modalInstance.result.then(function() {
                $scope.listUsers();
            }, function() {
                // Cancelled
            })['finally'](function(){
                $scope.modalInstance = undefined;
            });
        }

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

    }])

    .controller('AreaCtrl', ['$scope', 'AreaService',
        function ($scope, AreaService) {
        'use strict';

        $scope.areas = [];

        // TODO: TEST - REMOVE
        $scope.locations = [];
        $scope.fileUploaded = function(result) {
            console.log("******* FILE UPLOADED " + result[0]);
        };
        $scope.fileError = function(status, statusText) {
            console.error("******* FILE ERROR " + status + ", " + statusText);
        };

        $scope.options = {
            dropped: function(event) {
                $scope.changes = true;
                return true;
            }
        };

        $scope.changes = false;

        $scope.loadAreas = function() {
            AreaService.getAreas(
                function (data) {
                    $scope.areas = data;
                    $scope.changes = false;
                },
                function () {
                    console.log("Error clearing all caches");
                });
        };

        $scope.getRootNodesScope = function() {
            return angular.element(document.getElementById("area-tree-root")).scope();
        };

        $scope.collapseAll = function() {
            $scope.getRootNodesScope().collapseAll();
        };

        $scope.expandAll = function() {
            $scope.getRootNodesScope().expandAll();
        };

        $scope.toggle = function(scope) {
            scope.toggle();
        };

        $scope.removeArea = function(scope) {
            scope.remove();
            $scope.changes = true;
        };

        $scope.editArea = function(scope) {
            $scope.changes = true;
        };

        $scope.addChildArea = function(scope) {
            var nodeData = scope.$modelValue;
            nodeData.childAreas.push({
                id: nodeData.id * 10 + nodeData.childAreas.length,
                nameEnglish: nodeData.nameEnglish + '.' + (nodeData.childAreas.length + 1),
                nameDanish: 'N/A',
                childAreas: []
            });
            $scope.changes = true;
        };

    }]);
