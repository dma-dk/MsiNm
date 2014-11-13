
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

            getParamTypeNames: function(success, error) {
                $http.get('/rest/templates/param-type-names')
                    .success(success)
                    .error(error);
            },

            getListParamTypes: function(success, error) {
                $http.get('/rest/templates/list-param-types?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            createListParamType: function(parameterType, success, error) {
                $http.post('/rest/templates/list-param-type', parameterType)
                    .success(success)
                    .error(error);
            },

            updateListParamType: function(parameterType, success, error) {
                $http.put('/rest/templates/list-param-type', parameterType)
                    .success(success)
                    .error(error);
            },

            deleteListParamType: function(parameterType, success, error) {
                $http.delete('/rest/templates/list-param-type/' + parameterType.id)
                    .success(success)
                    .error(error);
            },

            getCompositeParamTypes: function(success, error) {
                $http.get('/rest/templates/composite-param-types')
                    .success(success)
                    .error(error);
            },

            createCompositeParamType: function(parameterType, success, error) {
                $http.post('/rest/templates/composite-param-type', parameterType)
                    .success(success)
                    .error(error);
            },

            updateCompositeParamType: function(parameterType, success, error) {
                $http.put('/rest/templates/composite-param-type', parameterType)
                    .success(success)
                    .error(error);
            },

            deleteCompositeParamType: function(parameterType, success, error) {
                $http.delete('/rest/templates/composite-param-type/' + parameterType.id)
                    .success(success)
                    .error(error);
            }

        };
    }]);



