
/**
 * The main controllers for the admin app.
 */
angular.module('msinm.admin')

    /**
     * ********************************************************************************
     * AdminUserCtrl
     * ********************************************************************************
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
            $scope.user = { email:undefined, firstName: undefined, lastName: undefined, roles: ['user'], activationEmail: true };
            $scope.userDlg();
        };

        $scope.editUser = function (user) {
            $scope.userAction = 'edit';
            $scope.user = angular.copy(user);
            $scope.user.activationEmail = false;
            $scope.userDlg();
        };

        $scope.canEditUser = function (user) {
            if ($.inArray('sysadmin', user.roles) > -1 && !$scope.hasRole('sysadmin')) {
                return false;
            } else if ($.inArray('admin', user.roles) > -1 && !($scope.hasRole('admin') || $scope.hasRole('sysadmin'))) {
                return false;
            }
            return true;
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
     * ********************************************************************************
     * AdminMailingListCtrl
     * ********************************************************************************
     * The UserMailingListCtrl handles mailing list subscription for the administrator
     */
    .controller('AdminMailingListCtrl', ['$scope', '$modal', 'MailingListService', 'DialogService',
        function ($scope, $modal, MailingListService, DialogService) {
            'use strict';

            $scope.error = undefined;
            $scope.mailingLists = [];

            $scope.loadMailingLists = function () {
                MailingListService.getAllMailingLists(
                    function(data) {
                        $scope.mailingLists = data;
                    },
                    function(data) {
                        $scope.error = "Error: " + data + ".";
                    }
                );
            };

            $scope.manageRecipients = function (mailList) {
                $scope.modalInstance = $modal.open({
                    controller: "ManageMailingListCtrl",
                    templateUrl : "/partials/user/manage-mailing-list-dialog.html",
                    resolve: {
                        mailList: function(){ return mailList; }
                    }
                });

                $scope.modalInstance.result.then(function() {
                    $scope.loadMailingLists();
                }, function() {
                    // Cancelled
                })['finally'](function(){
                    $scope.modalInstance = undefined;
                });
            };

            $scope.deleteMailingList = function (mailList) {
                DialogService.showConfirmDialog(
                    "Delete Mailing List?", "Delete mailing list '" + mailList.name + "'?")
                    .then(function() {
                        MailingListService.deleteMailingList(
                            mailList.id,
                            function(data) {
                                $scope.loadMailingLists();
                            },
                            function(data) {
                                $scope.error = "Error: " + data + ".";
                            }
                        );
                    });
            }
        }])

    /**
     * ********************************************************************************
     * ManageMailingListCtrl
     * ********************************************************************************
     * The ManageMailingListCtrl is used in the dialog that manages mailing list recipients
     */
    .controller('ManageMailingListCtrl', ['$scope', '$timeout', 'MailingListService', 'Auth', 'mailList',
        function ($scope, $timeout, MailingListService, Auth, mailList) {
            'use strict';

            $scope.mailList = angular.copy(mailList);
            if (!$scope.mailList.recipients) {
                $scope.mailList.recipients = [];
            }

            $scope.mailList.newRecipient = undefined;

            $scope.init = function () {
                $timeout(function () {
                    initUserField("#newRecipient", false, Auth);
                }, 500);
            };

            $scope.addNewRecipient = function () {
                if (!$scope.mailList.newRecipient) {
                    return;
                }
                if ($.inArray($scope.mailList.newRecipient, $scope.mailList.recipients) >= 0) {
                    $scope.mailList.newRecipient = undefined;
                    $("#newRecipient").select2('data', null);
                    return;
                }
                $scope.mailList.recipients.push($scope.mailList.newRecipient);
                $scope.mailList.newRecipient = undefined;
                $("#newRecipient").select2('data', null);
            };

            $scope.deleteRecipient = function (email) {
                $scope.mailList.recipients.splice($.inArray(email, $scope.mailList.recipients), 1);
            };

            $scope.update = function () {
                MailingListService.updateMailingList(
                    $scope.mailList,
                    function(data) {
                        $scope.$close('closed');
                    },
                    function(data) {
                        $scope.error = "Error: " + data + ".";
                    }
                );
            }
        }])


    /**
     * ********************************************************************************
     * OperationsCtrl
     * ********************************************************************************
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
     * ********************************************************************************
     * AreaCtrl
     * ********************************************************************************
     * Area Controller
     */
    .controller('AreaCtrl', ['$scope', 'growlNotifications', 'LangService', 'AreaService', 'DialogService',
        function ($scope, growlNotifications, LangService, AreaService, DialogService) {
        'use strict';

        $scope.areas = [];
        $scope.area = undefined;
        $scope.editArea = undefined;
        $scope.action = "edit";

        $scope.locationsVisible = false;

        // Used to ensure that description entities have a "name" field
        function ensureNameField(desc) {
            desc.name = '';
        }

        $scope.loadAreas = function() {
            AreaService.getAreas(
                function (data) {
                    $scope.areas = data;

                    $scope.area = undefined;
                    $scope.editArea = undefined;
                    $scope.areaForm.$setPristine();
                },
                function () {
                    console.log("Error fetching areas");
                });
        };

        $scope.newArea = function() {
            $scope.action = "add";
            $scope.editArea = LangService.checkDescs({ locations: [] }, ensureNameField);
            if ($scope.area) {
                $scope.editArea.parent = { id: $scope.area.id };
            }
            $scope.areaForm.$setPristine()
        };

        $scope.selectArea = function (area) {
            AreaService.getArea(
                area,
                function(data) {
                    $scope.action = "edit";
                    $scope.area = LangService.checkDescs(data, ensureNameField);
                    if (!$scope.area.locations) {
                        $scope.area.locations = [];
                    }
                    $scope.editArea = angular.copy($scope.area);
                    $scope.areaForm.$setPristine();
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function (data) {
                    console.log("Error fetching area");
                }
            )
        };

        $scope.moveArea = function (area, parent) {

            // Get confirmation
            DialogService.showConfirmDialog(
                "Move Area?", "Move " + area.descs[0].name + " to " + ((parent) ? parent.descs[0].name : "the root") + "?")
                .then(function() {
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
                });
        };

        $scope.changeSortOrder = function (moveUp) {
            AreaService.changeSortOrder(
                $scope.area.id,
                moveUp,
                function (data) {
                    $scope.loadAreas();
                },
                function (data) {
                    console.error("ERROR " + data);
                }
            )
        };

        $scope.recomputeTreeSortOrder = function () {
            AreaService.recomputeTreeSortOrder(
                function (data) {
                    growlNotifications.add('<h4>Tree Sort Order Updated</h4>', 'info', 3000);
                },
                function (data) {
                    growlNotifications.add('<h4>Updating Tree Sort Order Failed</h4>', 'danger', 3000);
                }
            )
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

            // Get confirmation
            DialogService.showConfirmDialog(
                    "Delete Area?", "Delete area " + $scope.area.descs[0].name + "?")
                .then(function() {
                    AreaService.deleteArea(
                        $scope.editArea,
                        function (data) {
                            $scope.loadAreas();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        }

    }])


    /**
     * ********************************************************************************
     * ChartCtrl
     * ********************************************************************************
     * Chart Controller
     */
    .controller('ChartCtrl', ['$scope', 'DialogService', 'ChartService',
        function ($scope, DialogService, ChartService) {
        'use strict';

        $scope.allCharts = [];

        // Pagination
        $scope.charts = [];
        $scope.pageSize = 20;
        $scope.currentPage = 1;
        $scope.chartNo = 0;

        $scope.search = '';
        $scope.chart = undefined;
        $scope.action = "edit";

        $scope.$watch(
            function() { return $scope.search; },
            function() { $scope.pageChanged(); },
            true);

        $scope.loadCharts = function() {
            ChartService.getCharts(
                function (data) {
                    $scope.allCharts = data;
                    $scope.chart = undefined;
                    $scope.pageChanged();
                },
                function () {
                    console.log("Error fetching charts");
                });
        };

        $scope.pageChanged = function() {
            var search = $scope.search.toLowerCase();
            var filteredCharts = $scope.allCharts.filter(function (chart) {
                return ("" + chart.chartNumber).contains(search) ||
                    ("" + chart.internationalNumber).contains(search) ||
                    ("" + chart.horizontalDatum).toLowerCase().contains(search) ||
                    ("" + chart.name).toLowerCase().contains(search);
            });
            $scope.chartNo = filteredCharts.length;
            $scope.charts = filteredCharts.slice(
                    $scope.pageSize * ($scope.currentPage - 1),
                    Math.min($scope.chartNo, $scope.pageSize * $scope.currentPage));
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
            var modalOptions = {
                closeButtonText: 'Cancel',
                actionButtonText: ($scope.action == 'add') ? 'Create' : 'Update',
                headerText: ($scope.action == 'add') ? 'Create Chart' : 'Edit Chart',
                chart: $scope.chart,
                action: $scope.action,
                templateUrl: "addEditChart.html"
            };

            DialogService.showDialog({}, modalOptions).then(function (result) {
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

        $scope.deleteChart = function (chart) {

            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Chart?", "Delete chart " + chart.chartNumber + "?")
                .then(function() {
                    ChartService.deleteChart(
                        chart,
                        function (data) {
                            $scope.loadCharts();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        }

    }])

    /**
     * ********************************************************************************
     * CategoryCtrl
     * ********************************************************************************
     * Category Controller
     */
    .controller('CategoryCtrl', ['$scope', 'LangService', 'CategoryService', 'DialogService',
        function ($scope, LangService, CategoryService, DialogService) {
        'use strict';

        $scope.categories = [];
        $scope.category = undefined;
        $scope.editCategory = undefined;
        $scope.action = "edit";

        // Used to ensure that description entities have a "name" field
        function ensureNameField(desc) {
            desc.name = '';
        }

        $scope.loadCategories = function() {
            CategoryService.getCategories(
                function (data) {
                    $scope.categories = data;

                    $scope.category = undefined;
                    $scope.editCategory = undefined;
                    $scope.categoryForm.$setPristine();
                },
                function () {
                    console.log("Error fetching categories");
                });
        };

        $scope.newCategory = function() {
            $scope.action = "add";
            $scope.editCategory = LangService.checkDescs({ }, ensureNameField);
            if ($scope.category) {
                $scope.editCategory.parent = { id: $scope.category.id };
            }
            $scope.categoryForm.$setPristine()
        };

        $scope.selectCategory = function (category) {
            CategoryService.getCategory(
                category,
                function(data) {
                    $scope.action = "edit";
                    $scope.category = LangService.checkDescs(data, ensureNameField);
                    $scope.editCategory = angular.copy($scope.category);
                    $scope.categoryForm.$setPristine();
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function (data) {
                    console.log("Error fetching category");
                }
            )
        };

        $scope.moveCategory = function (category, parent) {

            // Get confirmation
            DialogService.showConfirmDialog(
                "Move Category?", "Move " + category.descs[0].name + " to " + ((parent) ? parent.descs[0].name : "the root") + "?")
                .then(function() {
                    CategoryService.moveCategory(
                        category.id,
                        (parent) ? parent.id : undefined,
                        function (data) {
                            $scope.loadCategories();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };

        $scope.saveCategory = function () {
            if ($scope.action == 'add') {
                CategoryService.createCategory(
                    $scope.editCategory,
                    function (data) {
                        $scope.loadCategories();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )

            } else {
                CategoryService.updateCategory(
                    $scope.editCategory,
                    function (data) {
                        $scope.loadCategories();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )
            }
        };

        $scope.deleteCategory = function () {

            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Category?", "Delete category " + $scope.category.descs[0].name + "?")
                .then(function() {
                    CategoryService.deleteCategory(
                        $scope.editCategory,
                        function (data) {
                            $scope.loadCategories();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        }

    }])


    /**
     * ********************************************************************************
     * SettingsCtrl
     * ********************************************************************************
     * Settings Controller
     */
    .controller('SettingsCtrl', ['$scope', 'DialogService', 'SettingsService',
        function ($scope, DialogService, SettingsService) {
        'use strict';

        $scope.settings = [];

        $scope.loadSettings = function() {
            SettingsService.getSettings(
                function (data) {
                    $scope.settings = data;
                },
                function () {
                    console.log("Error fetching settings");
                });
        };

        $scope.editSetting = function (setting) {
            $scope.setting = angular.copy(setting);
            $scope.settingDlg();
        };

        $scope.settingDlg = function () {
            var modalOptions = {
                closeButtonText: 'Cancel',
                actionButtonText: 'Update',
                headerText: 'Edit Setting',
                setting: $scope.setting,
                templateUrl: "editSetting.html"
            };

            DialogService.showDialog({}, modalOptions).then(function (result) {
                SettingsService.updateSetting(
                    $scope.setting,
                    function (data) {
                        $scope.loadSettings();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )

            });

        };

    }])


    /**
     * ********************************************************************************
     * PublisherCtrl
     * ********************************************************************************
     * Publisher Controller
     */
    .controller('PublisherCtrl', ['$scope', 'DialogService', 'PublisherService',
        function ($scope, DialogService, PublisherService) {
            'use strict';

        $scope.publishers = [];

        $scope.loadPublishers = function () {
            PublisherService.getPublishers(
                function(data) {
                    $scope.publishers = data;
                },
                function () {
                    console.error("Error loading publishers");
                });
        };

        $scope.editPublisher = function (publisher) {
            var modalOptions = {
                closeButtonText: 'Cancel',
                actionButtonText: 'Update',
                headerText: 'Edit Publisher',
                publisher: angular.copy(publisher),
                templateUrl: "editPublisher.html"
            };

            DialogService.showDialog({}, modalOptions).then(function (result) {
                console.log("PUB " + modalOptions.publisher.active);
                PublisherService.updatePublisher(
                    modalOptions.publisher,
                    function (data) {
                        $scope.loadPublishers();
                    },
                    function (data) {
                        console.error("ERROR " + data);
                    }
                )

            });

        };

    }]);

