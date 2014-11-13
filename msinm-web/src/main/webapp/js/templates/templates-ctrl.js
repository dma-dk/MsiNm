/**
 * The main controllers for the templates functionality.
 */
angular.module('msinm.templates')

    /**
     * ********************************************************************************
     * TemplatesCtrl
     * ********************************************************************************
     * The TemplatesCtrl handles templates for the administrator
     */
    .controller('TemplatesCtrl', ['$scope', '$modal', 'TemplatesService', 'DialogService',
        function ($scope, $modal, TemplatesService, DialogService) {
        'use strict';

        $scope.error = undefined;
        $scope.templates = [];
        $scope.listParamTypes = [];
        $scope.compositeParamTypes = [];


        // Loads templates and parameter types
        $scope.loadAll = function () {
            //$scope.loadTemplates();
            $scope.loadListParamTypes();
            $scope.loadCompositeParamTypes();
        };

        // ****************************************
        // Templates functionality
        // ****************************************

        // Loads the templates
        $scope.loadTemplates = function () {
             // Load all templates, then load the parameter types
             TemplatesService.getTemplates(
             function (data) {
                $scope.templates = data;
            },
             function () {
                console.log("Error fetching templates");
            });
        };


        // Adds a new template
        $scope.addTemplate = function () {
            $scope.templateDlg(
                {   name:'',
                    categories: {},
                    parameters: [ { name: 'Ship Type', type: 'ShipType', mandatory: true } ],
                    fieldTemplates: [
                        { field : 'title:en', fmTemplate: '${msg.categories[0].descs[0].name}' }, { field : 'title:da', fmTemplate: '${msg.categories[0].descs[0].name}' },
                        { field : 'description:en', fmTemplate: '' }, { field : 'description:da', fmTemplate: '' }
                    ]
                },
                'add'
            );
        };


        // Edits the given template
        $scope.editTemplate = function (template) {
            $scope.templateDlg(
                template,
                'edit'
            );
        };


        // Deletes the given template after confirmation
        $scope.deleteTemplate = function (template) {
            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Template?", "Delete template " + template.name + "?")
                .then(function() {
                    TemplatesService.deleteTemplate(
                        template,
                        function (data) {
                            $scope.loadTemplates();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };


        // Opens the template in the template editor dialog
        $scope.templateDlg = function (template, userAction) {
            $scope.modalInstance = $modal.open({
                controller: "TemplatesDialogCtrl",
                templateUrl : "/partials/templates/templates-dialog.html",
                size: 'lg',
                resolve: {
                    template: function(){
                        return template;
                    },
                    userAction: function(){
                        return userAction;
                    }
                }
            });

            $scope.modalInstance.result.then(function() {
                $scope.loadTemplates();
            }, function() {
                // Cancelled
            })['finally'](function(){
                $scope.modalInstance = undefined;
            });
        };


        // ****************************************
        // List parameter type functionality
        // ****************************************

        // Loads the list parameter types
        $scope.loadListParamTypes = function () {
            TemplatesService.getListParamTypes(
                function (data) {
                    $scope.listParamTypes = data;
                },
                function () {
                    console.log("Error fetching list parameter types");
                });
        };


        // Adds a new list parameter type
        $scope.addListParamType = function () {
            $scope.listParamTypeDlg(
                {   name:'', values:[] },
                'add'
            );
        };


        // Edits the given list parameter type
        $scope.editListParamType = function (parameterType) {
            $scope.listParamTypeDlg(
                parameterType,
                'edit'
            );
        };


        // Deletes the given parameter type after confirmation
        $scope.deleteListParamType = function (parameterType) {
            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Parameter Type?", "Delete parameter type " + parameterType.name + "?")
                .then(function() {
                    TemplatesService.deleteListParamType(
                        parameterType,
                        function (data) {
                            $scope.loadListParamTypes();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };


        // Opens the list parameter type editor dialog
        $scope.listParamTypeDlg = function (parameterType, userAction) {
            $scope.modalInstance = $modal.open({
                controller: "ListParamTypeDialogCtrl",
                templateUrl : "/partials/templates/list-param-type-dialog.html",
                resolve: {
                    parameterType: function(){
                        return parameterType;
                    },
                    userAction: function(){
                        return userAction;
                    }
                }
            });

            $scope.modalInstance.result.then(function() {
                $scope.loadListParamTypes();
            }, function() {
                // Cancelled
            })['finally'](function(){
                $scope.modalInstance = undefined;
            });
        };


        // ****************************************
        // Composite parameter type functionality
        // ****************************************

        // Loads the composite parameter types
        $scope.loadCompositeParamTypes = function () {
            TemplatesService.getCompositeParamTypes(
                function (data) {
                    $scope.compositeParamTypes = data;
                },
                function () {
                    console.log("Error fetching composite parameter types");
                });
        };


        // Adds a new composite parameter type
        $scope.addCompositeParamType = function () {
            $scope.compositeParamTypeDlg(
                {   name:'', parameters:[] },
                'add'
            );
        };


        // Edits the given composite parameter type
        $scope.editCompositeParamType = function (parameterType) {
            $scope.compositeParamTypeDlg(
                parameterType,
                'edit'
            );
        };


        // Deletes the given parameter type after confirmation
        $scope.deleteCompositeParamType = function (parameterType) {
            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Parameter Type?", "Delete parameter type " + parameterType.name + "?")
                .then(function() {
                    TemplatesService.deleteCompositeParamType(
                        parameterType,
                        function (data) {
                            $scope.loadCompositeParamTypes();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };


        // Opens the composite parameter type editor dialog
        $scope.compositeParamTypeDlg = function (parameterType, userAction) {
            $scope.modalInstance = $modal.open({
                controller: "CompositeParamTypeDialogCtrl",
                templateUrl : "/partials/templates/composite-param-type-dialog.html",
                resolve: {
                    parameterType: function(){
                        return parameterType;
                    },
                    userAction: function(){
                        return userAction;
                    }
                }
            });

            $scope.modalInstance.result.then(function() {
                $scope.loadCompositeParamTypes();
            }, function() {
                // Cancelled
            })['finally'](function(){
                $scope.modalInstance = undefined;
            });
        };

    }])


    /**
     * ********************************************************************************
     * TemplatesDialogCtrl
     * ********************************************************************************
     * The TemplatesDialogCtrl is the Templates add/edit dialog controller
     */
    .controller('TemplatesDialogCtrl', ['$scope', '$modalInstance', 'TemplatesService', 'userAction', 'template',
        function ($scope, $modalInstance, TemplatesService, userAction, template) {
        'use strict';

        $scope.userAction = userAction;
        $scope.template = angular.copy(template);
        $scope.focusMe = true;


        // Creates or updates the template
        $scope.createOrUpdateTemplate = function() {
            if ($scope.userAction == 'add') {
                $scope.createTemplate();
            } else {
                $scope.updateTemplate();
            }
        };


        // Creates the template
        $scope.createTemplate = function() {
            TemplatesService.createTemplate(
                $scope.template,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the template data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };


        // Updates the template
        $scope.updateTemplate = function() {
            TemplatesService.updateTemplate(
                $scope.template,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the template data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

    }])


    /**
     * ********************************************************************************
     * ListParamTypeDialogCtrl
     * ********************************************************************************
     * The ListParamTypeDialogCtrl is the parameter type add/edit dialog controller
     */
    .controller('ListParamTypeDialogCtrl', ['$scope', '$modalInstance', 'LangService', 'TemplatesService', 'userAction', 'parameterType',
        function ($scope, $modalInstance, LangService, TemplatesService, userAction, parameterType) {
        'use strict';

        $scope.userAction = userAction;
        $scope.parameterType = angular.copy(parameterType);
        $scope.focusMe = true;


        // Ensures that the parameter type descriptor contains the mandatory fields
        function ensureValueFields(desc) {
            desc.shortValue = '';
            desc.longValue = '';
        }


        // Checks that the parameter values array is not empty and updates the sortKey of all values
        $scope.checkValues = function () {
            if ($scope.parameterType.values.length == 0) {
                $scope.parameterType.values.push(LangService.checkDescs({ }, ensureValueFields));
            }
            var sortKey = 1;
            for (var v in $scope.parameterType.values) {
                var val = $scope.parameterType.values[v];
                val.sortKey = sortKey++;
                LangService.checkDescs(val, ensureValueFields);
            }
        };

        $scope.checkValues();


        // Adds a new empty value below the given value
        $scope.addValueBelow = function (val) {
            var index = $.inArray(val, $scope.parameterType.values);
            if (index >= 0) {
                $scope.parameterType.values.splice(index + 1, 0, LangService.checkDescs({ }, ensureValueFields));
                $scope.checkValues();
            }
        };


        // Deletes the given value from the parameter value list
        $scope.deleteValue = function (val) {
            var index = $.inArray(val, $scope.parameterType.values);
            if (index >= 0) {
                $scope.parameterType.values.splice(index, 1);
                $scope.checkValues();
            }
        };


        // Moves the given value up or down in the parameter value list
        $scope.changeSortOrder = function(val, moveUp) {
            var index = $.inArray(val, $scope.parameterType.values);
            if ((moveUp && index > 0) || (!moveUp && index >= 0 && index < $scope.parameterType.values.length - 1)) {
                $scope.parameterType.values.splice(index, 1);
                $scope.parameterType.values.splice(index + (moveUp ? -1 : 1), 0, val);
                $scope.checkValues();
            }
        };


        // Creates or updates the list parameter type
        $scope.createOrUpdateParameterType = function() {
            if ($scope.userAction == 'add') {
                $scope.createParameterType();
            } else {
                $scope.updateParameterType();
            }
        };


        // Creates the list parameter type
        $scope.createParameterType = function() {
            TemplatesService.createListParamType(
                $scope.parameterType,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the parameter type data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };


        // Updates the list parameter type
        $scope.updateParameterType = function() {
            TemplatesService.updateListParamType(
                $scope.parameterType,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the parameter type data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

    }])


    /**
     * ********************************************************************************
     * CompositeParamTypeDialogCtrl
     * ********************************************************************************
     * The CompositeParamTypeDialogCtrl is the parameter type add/edit dialog controller
     */
    .controller('CompositeParamTypeDialogCtrl', ['$scope', '$modalInstance', 'LangService', 'TemplatesService', 'userAction', 'parameterType',
        function ($scope, $modalInstance, LangService, TemplatesService, userAction, parameterType) {
        'use strict';

        $scope.userAction = userAction;
        $scope.parameterType = angular.copy(parameterType);
        $scope.focusMe = true;


        // Creates or updates the composite parameter type
        $scope.createOrUpdateParameterType = function() {
            if ($scope.userAction == 'add') {
                $scope.createParameterType();
            } else {
                $scope.updateParameterType();
            }
        };


        // Creates the composite parameter type
        $scope.createParameterType = function() {
            TemplatesService.createCompositeParamType(
                $scope.parameterType,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the parameter type data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };


        // Updates the composite parameter type
        $scope.updateParameterType = function() {
            TemplatesService.updateCompositeParamType(
                $scope.parameterType,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the parameter type data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

    }]);

