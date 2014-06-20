
/**
 * The main controllers for the admin app.
 */
angular.module('msinm.admin')

    /**
     * Legacy Controller
     */
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


    /**
     * Admin User Controller
     */
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


    /**
     * Operations Controller
     */
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


    /**
     * Area Controller
     */
    .controller('AreaCtrl', ['$scope', 'AreaService',
        function ($scope, AreaService) {
        'use strict';

        $scope.areas = [];
        $scope.area = undefined;
        $scope.editArea = undefined;
        $scope.action = "edit";

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
                    console.log("Error fetching areas");
                });
        };

        $scope.newArea = function() {
            $scope.action = "add";
            $scope.editArea = { nameEnglish: '', nameLocal: '', locations: [] };
            if ($scope.area) {
                $scope.editArea.parentId = $scope.area.id;
            }
            $scope.areaForm.$setPristine()
        };

        $scope.selectArea = function (area) {
            // We do not want to copy the entire childarea-tree
            $scope.action = "edit";
            $scope.area = area;
            $scope.editArea = angular.copy($scope.area);
            $scope.areaForm.$setPristine()
            if(!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.moveArea = function (area, parent) {
            // Get confirmation
            if (confirm("Move " + area.nameEnglish + " to " + ((parent) ? parent.nameEnglish : "the root") + "?")) {
                AreaService.moveArea(
                    area.id,
                    (parent) ? parent.id : undefined,
                    function (data) {
                        $scope.loadAreas();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )
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
            if ($scope.action == 'add') {
                AreaService.createArea(
                    $scope.editArea,
                    function (data) {
                        $scope.loadAreas();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )

            } else {
                AreaService.updateArea(
                    $scope.editArea,
                    function (data) {
                        $scope.loadAreas();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )
            }
        };

        $scope.deleteArea = function () {
            if (confirm("Delete area " + $scope.area.nameEnglish + "?")) {
                AreaService.deleteArea(
                    $scope.editArea,
                    function (data) {
                        $scope.loadAreas();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )
            }
        }

    }])


    /**
     * Chart Controller
     */
    .controller('ChartCtrl', ['$scope', '$modal', 'ChartService',
        function ($scope, $modal, ChartService) {
        'use strict';

        $scope.charts = [];
        $scope.chart = undefined;
        $scope.action = "edit";
        var that = this;

        $scope.loadCharts = function() {
            ChartService.getCharts(
                function (data) {
                    $scope.charts = data;
                    $scope.chart = undefined;
                },
                function () {
                    console.log("Error fetching charts");
                });
        };

        $scope.addChart = function () {
            $scope.action = 'add';
            $scope.chart = { chartNumber: undefined, internationalNumber: undefined, horizontalDatum: undefined };
            $scope.chartDlg();
        };

        $scope.editChart = function (chart) {
            $scope.action = 'edit';
            $scope.chart = angular.copy(chart);
            $scope.chartDlg();
        };


        $scope.chartDlg = function () {
            $scope.modalInstance = $modal.open({
                controller: that,
                templateUrl : "/add-edit-chart.html"
            });

            $scope.modalInstance.result.then(function() {
                $scope.loadCharts();
            }, function() {
                // Cancelled
            })['finally'](function(){
                $scope.modalInstance = undefined;
            });
        };

        $scope.saveChart = function () {
            if ($scope.action == 'add') {
                ChartService.createChart(
                    $scope.chart,
                    function (data) {
                        $scope.loadCharts();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )

            } else {
                ChartService.updateChart(
                    $scope.chart,
                    function (data) {
                        $scope.loadCharts();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )
            }
        };

        $scope.deleteChart = function () {
            if (confirm("Delete chart " + $scope.chart.chartNumber + "?")) {
                ChartService.deleteChart(
                    $scope.chart,
                    function (data) {
                        $scope.loadCharts();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )
            }
        }

    }]);
