
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
                $http.get('/rest/messages/message/' + id + '?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            search: function(query, status, type, loc, areas, dateFrom, dateTo, maxHits, startIndex, sortBy, sortOrder, mapMode, success, error) {
                $http.get(
                        '/rest/messages/search?lang=' + $rootScope.language
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
                             + '&mapMode=' + mapMode
                    )
                    .success(success)
                    .error(error);
            }
        };
    }]);
