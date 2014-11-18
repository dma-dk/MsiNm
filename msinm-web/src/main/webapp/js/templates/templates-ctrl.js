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
    .controller('TemplatesCtrl', ['$scope', '$modal', 'LangService', 'TemplatesService', 'DialogService',
        function ($scope, $modal, LangService, TemplatesService, DialogService) {
        'use strict';

        $scope.error = undefined;
        $scope.templates = [];
        $scope.listParamTypes = [];
        $scope.compositeParamTypes = [];
        $scope.dictTerms = [];
        $scope.fmIncludes = [];


        // Loads templates and parameter types
        $scope.loadAll = function () {
            $scope.loadTemplates();
            $scope.loadListParamTypes();
            $scope.loadCompositeParamTypes();
            $scope.loadDictTerms();
            $scope.loadFmIncludes();
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
                    parameters: [],
                    fieldTemplates: []
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


        // Copies the given template
        $scope.copyTemplate = function (template) {
            var tmpl = angular.copy(template);
            delete tmpl.id;
            tmpl.name = '';
            $scope.templateDlg(
                tmpl,
                'add'
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
                {   name:'', kind:'LIST', values:[] },
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
                {   name:'', kind:'COMPOSITE', parameters:[] },
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


        // ****************************************
        // Dictionary functionality
        // ****************************************

        // Loads the list dictionary terms
        $scope.loadDictTerms = function () {
            TemplatesService.getDictTerms(
                function (data) {
                    $scope.dictTerms = data;
                },
                function () {
                    console.log("Error fetching dictionary terms");
                });
        };


        // Ensures that the dictionary term descriptor contains the mandatory fields
        function ensureDictTermFields(desc) {
            desc.value = '';
        }


        // Adds a new dictionary term
        $scope.addDictTerm = function () {
            $scope.dictTermDlg(
                LangService.checkDescs({ key:'' }, ensureDictTermFields),
                'add'
            );
        };


        // Edits the given dictionary term
        $scope.editDictTerm = function (dictTerm) {
            $scope.dictTermDlg(
                LangService.checkDescs(dictTerm, ensureDictTermFields),
                'edit'
            );
        };


        // Deletes the given dictionary term after confirmation
        $scope.deleteDictTerm = function (dictTerm) {
            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Dictionary Term?", "Delete Dictionary Term " + dictTerm.key + "?")
                .then(function() {
                    TemplatesService.deleteDictTerm(
                        dictTerm,
                        function (data) {
                            $scope.loadDictTerms();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };


        // Opens the dictionary term editor dialog
        $scope.dictTermDlg = function (dictTerm, userAction) {
            $scope.modalInstance = $modal.open({
                controller: "DictTermDialogCtrl",
                templateUrl : "/partials/templates/dict-term-dialog.html",
                resolve: {
                    dictTerm: function(){
                        return dictTerm;
                    },
                    userAction: function(){
                        return userAction;
                    }
                }
            });

            $scope.modalInstance.result.then(function() {
                $scope.loadDictTerms();
            }, function() {
                // Cancelled
            })['finally'](function(){
                $scope.modalInstance = undefined;
            });
        };


        // ****************************************
        // Freemarker Include functionality
        // ****************************************

        // Loads the Freemarker includes
        $scope.loadFmIncludes = function () {
            TemplatesService.getFmIncludes(
                function (data) {
                    $scope.fmIncludes = data;
                },
                function () {
                    console.log("Error fetching Freemarker includes");
                });
        };


        // Adds a new Freemarker include
        $scope.addFmInclude = function () {
            $scope.fmIncludeDlg(
                {   name:'',
                    fmTemplate: ''
                },
                'add'
            );
        };


        // Edits the given Freemarker include
        $scope.editFmInclude = function (fmInclude) {
            $scope.fmIncludeDlg(
                fmInclude,
                'edit'
            );
        };


        // Deletes the given Freemarker include after confirmation
        $scope.deleteFmInclude = function (fmInclude) {
            // Get confirmation
            DialogService.showConfirmDialog(
                "Delete Freemarker Include?", "Delete Freemarker include " + fmInclude.name + "?")
                .then(function() {
                    TemplatesService.deleteFmInclude(
                        fmInclude,
                        function (data) {
                            $scope.loadFmIncludes();
                        },
                        function (data) {
                            console.error("ERROR " + data);
                        }
                    )
                });
        };


        // Opens the Freemarker include in the editor dialog
        $scope.fmIncludeDlg = function (fmInclude, userAction) {
            $scope.modalInstance = $modal.open({
                controller: "FmIncludeDialogCtrl",
                templateUrl : "/partials/templates/fm-include-dialog.html",
                size: 'lg',
                resolve: {
                    fmInclude: function(){
                        return fmInclude;
                    },
                    userAction: function(){
                        return userAction;
                    }
                }
            });

            $scope.modalInstance.result.then(function() {
                $scope.loadFmIncludes();
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
    .controller('TemplatesDialogCtrl', ['$scope', '$modal', '$modalInstance', '$timeout', 'LangService', 'TemplatesService', 'userAction', 'template',
        function ($scope, $modal, $modalInstance, $timeout, LangService, TemplatesService, userAction, template) {
        'use strict';

        $scope.userAction = userAction;
        $scope.template = angular.copy(template);
        $scope.focusMe = true;
        $scope.fieldTemplates = [ ];

        if (!$scope.template.fieldTemplates) {
            $scope.template.fieldTemplates = [];
        }

        // Checks if the template contains the field template for the given field and language
        $scope.containsFieldTemplate = function(field, lang) {
            for (var f in $scope.template.fieldTemplates) {
                var tmpl = $scope.template.fieldTemplates[f];
                if (tmpl.field == field && tmpl.lang == lang) {
                    return true;
                }
            }
            return false;
        };


        // Sorts the field templates by sort key
        $scope.sortFieldTemplates = function () {
            $scope.template.fieldTemplates.sort(function(d1, d2){
                return d1.sortKey - d2.sortKey;
            });
        };

        // Load the field templates
        TemplatesService.getFieldTemplates(
            function (data) {
                for (var f in data) {
                    var tmpl = data[f];
                    if (!$scope.containsFieldTemplate(tmpl.field, tmpl.lang)) {
                        if ($scope.userAction == 'add' && tmpl.defaultField) {
                            $scope.template.fieldTemplates.push(tmpl);
                        } else {
                            $scope.fieldTemplates.push(tmpl);
                        }
                    }
                }
                $scope.sortFieldTemplates();
            },
            function (data) {
                console.error("Failed loading field templates")
            }
        );

        // Initialize the types field and the categories field
        $timeout(function () {
            initCategoryField("#templateCategories", true);
            if (template.categories && template.categories.length > 0) {
                var data = [];
                $scope.template.categoryIds = '';
                for (var i in template.categories) {
                    var cat = template.categories[i];
                    if ($scope.template.categoryIds != '') {
                        $scope.template.categoryIds += ',';
                    }
                    $scope.template.categoryIds += cat.id;
                    data.push({id: cat.id, text: LangService.descForLangOrDefault(cat).name, category: cat });
                }
                $("#templateCategories").select2("data", data);
            } else {
                $("#templateCategories").select2("data", null);
            }
        }, 100);


        // Adds the given field template to the template
        $scope.addFieldTemplate = function (fieldTemplate) {
            // Remove from the list of available field templates
            var index = $.inArray(fieldTemplate, $scope.fieldTemplates);
            $scope.fieldTemplates.splice(index, 1);

            // Add to the template
            $scope.template.fieldTemplates.push(fieldTemplate);
            $scope.sortFieldTemplates();
        };


        // Creates or updates the template
        $scope.createOrUpdateTemplate = function() {

            // Update Categories
            var categoryData = $("#templateCategories").select2("data");
            $scope.template.categories = [];
            for (var i in categoryData) {
                $scope.template.categories.push(categoryData[i].category);
                // Trim json
                categoryData[i].category.parent = null; // Trim json
            }

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


        // Test the current template
        $scope.test = function () {
            $modal.open({
                controller: "TestTemplateDialogCtrl",
                templateUrl : "/partials/templates/test-template-dialog.html",
                resolve: {
                    template: function(){
                        return $scope.template;
                    }
                }
            });

        };
    }])


    /**
     * ********************************************************************************
     * TestTemplateDialogCtrl
     * ********************************************************************************
     * The TestTemplateDialogCtrl is the template test dialog controller
     */
    .controller('TestTemplateDialogCtrl', ['$scope', '$modalInstance', '$cookieStore', 'TemplatesService', 'template',
        function ($scope, $modalInstance, $cookieStore, TemplatesService, template) {
        'use strict';

        $scope.template = angular.copy(template);
        $scope.paramData = {};
        $scope.messageId = $cookieStore.get('testMessageId');
        $scope.focusMe = true;

        if (!$scope.template.fieldTemplates) {
            $scope.template.fieldTemplates = [];
        }

        // Load the parameter types
        $scope.parameterTypes = {};
        TemplatesService.getParamTypes(
            function (data) {
                // Build a look-up map for param types
                for (var p in data) {
                    var paramType = data[p];
                    $scope.parameterTypes[paramType.name] = paramType;
                }
            },
            function (data) {
                console.error("Error loading parameter types");
            }
        );

        $scope.paramsValid = function (params) {
            var valid = true;
            for (var p in params) {
                var param = params[p];
                var paramType = $scope.parameterTypes[param.type];
                if (paramType && paramType.kind == 'COMPOSITE') {
                    valid = valid && $scope.paramsValid(paramType.parameters);
                } else {
                    valid = valid && (!param.mandatory || $scope.paramData[param.name] !== undefined);
                }
            }
            return valid;
        };

        // Test the current template
        $scope.test = function (messageId) {
            if (messageId) {
                $scope.messageId = messageId;
                $cookieStore.put('testMessageId', $scope.messageId);

                TemplatesService.processTemplate(
                    messageId.fullId,
                    $scope.template,
                    function (data) {
                        $scope.template = data;
                    },
                    function (data) {
                        $scope.error = "ERROR";
                    }
                )
            }
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

    }])


    /**
     * ********************************************************************************
     * DictTermDialogCtrl
     * ********************************************************************************
     * The DictTermDialogCtrl is the dictionary term add/edit dialog controller
     */
    .controller('DictTermDialogCtrl', ['$scope', '$modalInstance', 'TemplatesService', 'userAction', 'dictTerm',
        function ($scope, $modalInstance, TemplatesService, userAction, dictTerm) {
        'use strict';

        $scope.userAction = userAction;
        $scope.dictTerm = angular.copy(dictTerm);
        $scope.focusMe = true;


        // Creates or updates the dictionary term
        $scope.createOrUpdateDictTerm = function() {
            if ($scope.userAction == 'add') {
                $scope.createDictTerm();
            } else {
                $scope.updateDictTerm();
            }
        };


        // Creates the dictionary term
        $scope.createDictTerm = function() {
            TemplatesService.createDictTerm(
                $scope.dictTerm,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the dictionary term data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };


        // Updates the dictionary term
        $scope.updateDictTerm = function() {
            TemplatesService.updateDictTerm(
                $scope.dictTerm,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the dictionary term data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

    }])



    /**
     * ********************************************************************************
     * FmIncludeDialogCtrl
     * ********************************************************************************
     * The FmIncludeDialogCtrl is the Freemarker includes add/edit dialog controller
     */
    .controller('FmIncludeDialogCtrl', ['$scope', '$modalInstance', 'TemplatesService', 'userAction', 'fmInclude',
        function ($scope, $modalInstance, TemplatesService, userAction, fmInclude) {
        'use strict';

        $scope.userAction = userAction;
        $scope.fmInclude = angular.copy(fmInclude);
        $scope.focusMe = true;


        // Creates or updates the Freemarker include file
        $scope.createOrUpdateFmInclude = function() {
            if ($scope.userAction == 'add') {
                $scope.createFmInclude();
            } else {
                $scope.updateFmInclude();
            }
        };


        // Creates the Freemarker include file
        $scope.createFmInclude = function() {
            TemplatesService.createFmInclude(
                $scope.fmInclude,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the Freemarker template data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };


        // Updates the Freemarker include file
        $scope.updateFmInclude = function() {
            TemplatesService.updateFmInclude(
                $scope.fmInclude,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the Freemarker template data.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

    }]);


