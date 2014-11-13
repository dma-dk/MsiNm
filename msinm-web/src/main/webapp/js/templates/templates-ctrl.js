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
        $scope.parameterTypes = [];

        $scope.loadTemplates = function () {
            /*** TODO
             // Load all templates, then load the parameter types
             TemplatesService.getTemplates(
             function (data) {
                $scope.templates = data;
                TemplatesService.getParameterTypes(
                    function (data) {
                        $scope.parameterTypes = data;
                    },
                    function () {
                        console.log("Error fetching parameter types");
                    });
            },
             function () {
                console.log("Error fetching templates");
            });
             **/
            TemplatesService.getParameterTypes(
                function (data) {
                    $scope.parameterTypes = data;
                },
                function () {
                    console.log("Error fetching parameter types");
                });
        };

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

        $scope.addParameterType = function () {
            $scope.parameterTypeDlg(
                {   name:'', values:[] },
                'add'
            );
        };

        $scope.editParameterType = function (parameterType) {
            $scope.parameterTypeDlg(
                parameterType,
                'edit'
            );
        };

        // Deletes the given parameter type after confirmation
        $scope.deleteParameterType = function (parameterType) {
            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Parameter Type?", "Delete parameter type " + parameterType.name + "?")
                .then(function() {
                    TemplatesService.deleteParameterType(
                        parameterType,
                        function (data) {
                            $scope.loadTemplates();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };

        $scope.parameterTypeDlg = function (parameterType, userAction) {
            $scope.modalInstance = $modal.open({
                controller: "ParameterTypeDialogCtrl",
                templateUrl : "/partials/templates/templates-param-type-dialog.html",
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
                $scope.loadTemplates();
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

        $scope.createOrUpdateTemplate = function() {
            if ($scope.userAction == 'add') {
                $scope.createTemplate();
            } else {
                $scope.updateTemplate();
            }
        };

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
     * ParameterTypeDialogCtrl
     * ********************************************************************************
     * The ParameterTypeDialogCtrl is the parameter type add/edit dialog controller
     */
    .controller('ParameterTypeDialogCtrl', ['$scope', '$modalInstance', 'LangService', 'TemplatesService', 'userAction', 'parameterType',
        function ($scope, $modalInstance, LangService, TemplatesService, userAction, parameterType) {
        'use strict';

        $scope.userAction = userAction;
        $scope.parameterType = angular.copy(parameterType);
        $scope.focusMe = true;

        function ensureValueFields(desc) {
            desc.shortValue = '';
            desc.longValue = '';
        }

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

        $scope.addValueBelow = function (val) {
            var index = $.inArray(val, $scope.parameterType.values);
            if (index >= 0) {
                $scope.parameterType.values.splice(index + 1, 0, LangService.checkDescs({ }, ensureValueFields));
                $scope.checkValues();
            }
        };

        $scope.deleteValue = function (val) {
            var index = $.inArray(val, $scope.parameterType.values);
            if (index >= 0) {
                $scope.parameterType.values.splice(index, 1);
                $scope.checkValues();
            }
        };

        $scope.changeSortOrder = function(val, moveUp) {
            var index = $.inArray(val, $scope.parameterType.values);
            if ((moveUp && index > 0) || (!moveUp && index >= 0 && index < $scope.parameterType.values.length - 1)) {
                $scope.parameterType.values.splice(index, 1);
                $scope.parameterType.values.splice(index + (moveUp ? -1 : 1), 0, val);
                $scope.checkValues();
            }
        };

        $scope.createOrUpdateParameterType = function() {
            if ($scope.userAction == 'add') {
                $scope.createParameterType();
            } else {
                $scope.updateParameterType();
            }
        };

        $scope.createParameterType = function() {
            TemplatesService.createParameterType(
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

        $scope.updateParameterType = function() {
            TemplatesService.updateParameterType(
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


