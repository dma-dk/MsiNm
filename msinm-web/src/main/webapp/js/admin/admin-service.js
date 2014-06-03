
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.admin')


    .factory('LegacyService', [ '$http', '$location', function($http, $location) {
        'use strict';

        var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();

        return {

            importMsiNm: function(success, error) {
                $http.get(host + '/rest/import/legacy-ws-msi')
                    .success(success)
                    .error(error);
            },

            importLegacyMsi: function(count, success, error) {
                $http.get(host + '/rest/import/legacy-db-msi?limit=' + count)
                    .success(success)
                    .error(error);
            },

            recreateSearchIndex: function(success, error) {
                $http.get(host + '/rest/message/recreate-search-index')
                    .success(success)
                    .error(error);
            }
        };
    }]);
