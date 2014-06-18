
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
        $scope.area = undefined;
        $scope.editArea = undefined;

        $scope.locationsVisible = false;


        $scope.loadAreas = function() {
            AreaService.getAreas(
                function (data) {
                    //while($scope.areas.length > 0) $scope.areas.pop();
                    //for (var i in data) $scope.areas.push(data[i]);
                    $scope.areas = data;

                    $scope.area = undefined;
                    $scope.editArea = undefined;
                    $scope.areaForm.$setPristine()
                },
                function () {
                    console.log("Error clearing all caches");
                });
        };

        $scope.selectArea = function (area) {
            // We do not want to copy the entire childarea-tree
            $scope.area = area;
            $scope.editArea = angular.copy($scope.area);
            $scope.areaForm.$setPristine()
            if(!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.showLocations = function (show) {
            if (show) {
                $('.area-locations').fadeIn(0);
                $scope.locationsVisible = true;
            } else {
                $('.area-locations').fadeOut(0);
                $scope.locationsVisible = false;
            }
        };

        $scope.updateLocations = function (save) {
            $scope.showLocations(false);
            if (save) {
                $scope.areaForm.$setDirty();
            } else {
                angular.copy($scope.area.locations, $scope.editArea.locations);
            }
        };

        $scope.saveArea = function () {
            angular.copy($scope.editArea, $scope.area);
            $scope.areaForm.$setPristine();

            AreaService.updateArea(
                $scope.area,
                function (data) {
                    console.log("SUCCESS");
                },
                function (data) {
                    console.error("ERROR " + data);
                }
            )
        }
    }]);
