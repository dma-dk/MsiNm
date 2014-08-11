
/**
 * Services that retrieves MsiNm messages from the backend
 */
angular.module('msinm.search')

    /**
     * Interface for calling the application server
     */
    .factory('MessageService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        return {

            details: function(id, success, error) {
                $http.get('/rest/message/' + id + '?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            search: function(query, status, type, loc, areas, dateFrom, dateTo, maxHits, startIndex, sortBy, sortOrder, success, error) {
                $http.get(
                        '/rest/message/search?lang=' + $rootScope.language
                             + '&q=' + encodeURIComponent(query)
                             + '&status=' + encodeURIComponent(status)
                             + '&type=' + encodeURIComponent(type)
                             + '&loc=' + encodeURIComponent(loc)
                             + '&areas=' + encodeURIComponent(areas)
                             + '&from=' + encodeURIComponent(dateFrom)
                             + '&to=' + encodeURIComponent(dateTo)
                             + '&maxHits=' + maxHits
                             + '&startIndex=' + startIndex
                             + '&sortBy=' + sortBy
                             + '&sortOrder=' + sortOrder
                    )
                    .success(success)
                    .error(error);
            }
        };
    }]);
