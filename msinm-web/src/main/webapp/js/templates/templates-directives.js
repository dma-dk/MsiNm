/**
 * Templates directives.
 */
angular.module('msinm.templates')

    /**
     * Manages a list of template parameters
     */
    .directive('msiTemplateParamList', ['TemplatesService', function (TemplatesService) {
        return {
            restrict: 'E',
            templateUrl: '/partials/templates/template-param-list.html',
            replace: true,
            scope: {
                parameters: "="
            },
            link: function(scope, element, attrs) {

                // Returns a new empty parameter
                scope.emptyParameter = function () {
                    return { name: '', type:'text', mandatory: false, list: false, sortKey: 1 };
                };


                // Load the parameter type names
                scope.parameterTypes = [];
                TemplatesService.getParamTypeNames(
                    function (data) {
                        scope.parameterTypes = data;
                    },
                    function (data) {
                        console.error("Error loading parameter types");
                    });


                // Ensure that there is at least one parameter line to fill out
                // Re-index the sort keys
                scope.checkParameters = function () {
                    if (scope.parameters.length == 0) {
                        scope.parameters.push(scope.emptyParameter());
                    }
                    var sortKey = 1;
                    for (var p in scope.parameters) {
                        scope.parameters[p].sortKey = sortKey++;
                    }
                };

                scope.checkParameters();


                // Adds a new blank parameter below the given parameter in the composite parameter list
                scope.addParameterBelow = function (param) {
                    var index = $.inArray(param, scope.parameters);
                    if (index >= 0) {
                        scope.parameters.splice(index + 1, 0, scope.emptyParameter());
                        scope.checkParameters();
                    }
                };


                // Deletes the given parameter from the composite parameter list
                scope.deleteParameter = function (param) {
                    var index = $.inArray(param, scope.parameters);
                    if (index >= 0) {
                        scope.parameters.splice(index, 1);
                        scope.checkParameters();
                    }
                };


                // Moves the given parameter up or down in the composite parameter list
                scope.changeSortOrder = function(param, moveUp) {
                    var index = $.inArray(param, scope.parameters);
                    if ((moveUp && index > 0) || (!moveUp && index >= 0 && index < scope.parameters.length - 1)) {
                        scope.parameters.splice(index, 1);
                        scope.parameters.splice(index + (moveUp ? -1 : 1), 0, param);
                        scope.checkParameters();
                    }
                };
            }
        };
    }])


    /**
     * Manages user submitted template data based on a list of template parameters
     */
    .directive('msiTemplateParamData', ['TemplatesService', function (TemplatesService) {
        return {
            restrict: 'E',
            templateUrl: '/partials/templates/template-param-data.html',
            replace: true,
            scope: {
                parameters: "=",
                data: "="
            },
            link: function(scope, element, attrs) {

                // Load the parameter types
                scope.parameterTypes = {};
                TemplatesService.getParamTypes(
                    function (data) {
                        // Build a look-up map for param types
                        for (var p in data) {
                            var paramType = data[p];
                            scope.parameterTypes[paramType.name] = paramType;
                        }
                    },
                    function (data) {
                        console.error("Error loading parameter types");
                    }
                );



            }
        };
    }]);
