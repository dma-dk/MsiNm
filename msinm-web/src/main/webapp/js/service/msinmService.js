
/**
 * Services that retrieves MsiNm messages from the backend
 */
angular.module('msinm')
    .factory('MsiNmService', [ '$http', '$location', function($http, $location) {
        'use strict';

        return {
            search: function(query, success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(host + '/rest/message/search?q=' + encodeURIComponent(query))
                    .success(success)
                    .error(error);
            },

            importMsiNm: function(success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(host + '/rest/message/import-legacy-msi')
                    .success(success)
                    .error(error);
            }
        };
    }]);
