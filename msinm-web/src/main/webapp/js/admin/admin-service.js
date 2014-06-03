
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.admin')


    .factory('LegacyService', [ '$http', '$location', function($http, $location) {
        'use strict';

        return {

            importMsiNm: function(success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(host + '/rest/import/legacy-ws-msi')
                    .success(success)
                    .error(error);
            },

            importLegacyMsi: function(count, success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(host + '/rest/import/legacy-db-msi?limit=' + count)
                    .success(success)
                    .error(error);
            }
        };
    }]);
