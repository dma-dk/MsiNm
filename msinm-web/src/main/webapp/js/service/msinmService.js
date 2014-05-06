
/**
 * Services that retrieves MsiNm messages from the backend
 */
angular.module('msinm')
    .factory('MsiNmService', [ '$http', '$location', function($http, $location) {
        'use strict';

        return {
            search: function(query, status, type, loc, dateFrom, dateTo, maxHits, startIndex, success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(
                        host + '/rest/message/search?q=' + encodeURIComponent(query)
                             + '&status=' + encodeURIComponent(status)
                             + '&type=' + encodeURIComponent(type)
                             + '&loc=' + encodeURIComponent(loc)
                             + '&from=' + encodeURIComponent(dateFrom)
                             + '&to=' + encodeURIComponent(dateTo)
                             + '&maxHits=' + maxHits
                             + '&startIndex=' + startIndex
                    )
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
