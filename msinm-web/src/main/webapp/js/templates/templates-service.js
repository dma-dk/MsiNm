
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.templates')
    .factory('TemplatesService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        return {
            getTemplates: function(success, error) {
                $http.get('/rest/templates/all?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            createTemplate: function(template, success, error) {
                $http.post('/rest/templates/template', template)
                    .success(success)
                    .error(error);
            },

            updateTemplate: function(template, success, error) {
                $http.put('/rest/templates/template', template)
                    .success(success)
                    .error(error);
            },

            deleteTemplate: function(template, success, error) {
                $http.delete('/rest/templates/template/' + template.id)
                    .success(success)
                    .error(error);
            },

            getParameterTypes: function(success, error) {
                $http.get('/rest/templates/allParameterTypes?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            createParameterType: function(parameterType, success, error) {
                $http.post('/rest/templates/parameterType', parameterType)
                    .success(success)
                    .error(error);
            },

            updateParameterType: function(parameterType, success, error) {
                $http.put('/rest/templates/parameterType', parameterType)
                    .success(success)
                    .error(error);
            },

            deleteParameterType: function(parameterType, success, error) {
                $http.delete('/rest/templates/parameterType/' + parameterType.id)
                    .success(success)
                    .error(error);
            }

        };
    }]);



