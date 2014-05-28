
/**
 * Services that retrieves MsiNm messages from the backend
 */
angular.module('msinm.search')

    /**
     * Interface for calling the application server
     */
    .factory('SearchService', [ '$http', '$location', 'Auth', function($http, $location, Auth) {
        'use strict';

        return {
            search: function(query, status, type, loc, dateFrom, dateTo, maxHits, startIndex, sortBy, sortOrder, success, error) {
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
                            + '&sortBy=' + sortBy
                            + '&sortOrder=' + sortOrder
                    )
                    .success(success)
                    .error(error);
            },

            importMsiNm: function(success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(host + '/rest/message/import-legacy-msi')
                    .success(success)
                    .error(error);
            },

            importLegacyMsi: function(count, success, error) {
                var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();
                $http.get(host + '/rest/import/legacy_msi?limit=' + count)
                    .success(success)
                    .error(error);
            }
        };
    }]);
