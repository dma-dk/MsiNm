
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.templates')
    .factory('TemplatesService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        return {

            // *******************************
            // Template functionality
            // *******************************

            getTemplates: function(success, error) {
                $http.get('/rest/templates/all?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            getTemplate: function(name, success, error) {
                $http.get('/rest/templates/template/' + encodeURIComponent(name) + '?lang=' + $rootScope.language)
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

            getFieldTemplates: function(success, error) {
                $http.get('/rest/templates/field-templates')
                    .success(success)
                    .error(error);
            },

            getTemplateNamesForCategories: function(categoryIds, type, success, error) {
                $http.get('/rest/templates/category-templates?categoryIds='
                + categoryIds + "&type=" + type)
                    .success(success)
                    .error(error);
            },

            // *******************************
            // Parameter type functionality
            // *******************************

            getParamTypes: function(success, error) {
                $http.get('/rest/templates/param-types?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            getParamTypeNames: function(success, error) {
                $http.get('/rest/templates/param-type-names')
                    .success(success)
                    .error(error);
            },

            // *******************************
            // List Parameter type functionality
            // *******************************

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

            // *******************************
            // Composite type functionality
            // *******************************

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
            },

            // *******************************
            // Dictionary functionality
            // *******************************

            getDictTerms: function(success, error) {
                $http.get('/rest/templates/dict-terms?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            createDictTerm: function(dictTerm, success, error) {
                $http.post('/rest/templates/dict-term', dictTerm)
                    .success(success)
                    .error(error);
            },

            updateDictTerm: function(dictTerm, success, error) {
                $http.put('/rest/templates/dict-term', dictTerm)
                    .success(success)
                    .error(error);
            },

            deleteDictTerm: function(dictTerm, success, error) {
                $http.delete('/rest/templates/dict-term/' + dictTerm.id)
                    .success(success)
                    .error(error);
            },

            // *******************************
            // Freemarker functionality
            // *******************************

            getFmIncludes: function(success, error) {
                $http.get('/rest/templates/fm-includes')
                    .success(success)
                    .error(error);
            },

            createFmInclude: function(fmInclude, success, error) {
                $http.post('/rest/templates/fm-include', fmInclude)
                    .success(success)
                    .error(error);
            },

            updateFmInclude: function(fmInclude, success, error) {
                $http.put('/rest/templates/fm-include', fmInclude)
                    .success(success)
                    .error(error);
            },

            deleteFmInclude: function(fmInclude, success, error) {
                $http.delete('/rest/templates/fm-include/' + fmInclude.id)
                    .success(success)
                    .error(error);
            },

            processTemplate: function(msgId, template, params, success, error) {
                $http.post('/rest/templates/process-template', {
                        msgId : msgId,
                        params : params,
                        template : template
                    })
                    .success(success)
                    .error(error);
            },

            executeTemplate: function(message, template, params, success, error) {
                $http.post('/rest/templates/execute-template', {
                    message : message,
                    params : params,
                    template : template
                })
                    .success(success)
                    .error(error);
            }

        };
    }]);



