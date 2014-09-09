
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

            newMessageTemplate: function(success, error) {
                $http.get('/rest/messages/new-message-template')
                    .success(success)
                    .error(error);
            },

            copyMessageTemplate: function(id, ref, success, error) {
                $http.get('/rest/messages/copy-message-template/' + id + ((ref) ? "?reference=" + ref : ''))
                    .success(success)
                    .error(error);
            },

            listFiles: function(dir, success, error) {
                $http.get('/rest/repo/list/' + dir)
                    .success(success)
                    .error(error);
            },

            details: function(id, success, error) {
                $http.get('/rest/messages/message/' + id + '?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            allDetails: function(id, success, error) {
                $http.get('/rest/messages/message/' + id)
                    .success(success)
                    .error(error);
            },

            transformMessage: function(transform, success, error) {
                $http.post('/rest/messages/transform', transform)
                    .success(success)
                    .error(error);
            },

            translateTime: function(time, success, error) {
                $http.post('/rest/messages/translate-time', time)
                    .success(success)
                    .error(error);
            },

            intersectingCharts: function(locations, success, error) {
                $http.post('/rest/admin/charts/intersecting-charts', locations)
                    .success(success)
                    .error(error);
            },

            createMessage: function(msg, success, error) {
                $http.post('/rest/messages/message', msg)
                    .success(success)
                    .error(error);
            },

            updateMessage: function(msg, success, error) {
                $http.put('/rest/messages/message', msg)
                    .success(success)
                    .error(error);
            },

            updateMessageStatus: function(status, success, error) {
                $http.put('/rest/messages/update-status', status)
                    .success(success)
                    .error(error);
            },

            getMessageHistory: function(id, success, error) {
                $http.get('/rest/messages/history/' + id)
                    .success(success)
                    .error(error);
            },

            addBookmark: function(messageId, success, error) {
                $http.post('/rest/messages/bookmark/' + messageId, {})
                    .success(success)
                    .error(error);
            },

            removeBookmark: function(messageId, success, error) {
                $http.delete('/rest/messages/bookmark/' + messageId)
                    .success(success)
                    .error(error);
            },

            search: function(query, status, type, loc, areas, categories, charts, dateFrom, dateTo, maxHits, startIndex, sortBy, sortOrder, mapMode, success, error) {
                $http.get(
                        '/rest/messages/search?lang=' + $rootScope.language
                             + '&q=' + encodeURIComponent(query)
                             + '&status=' + encodeURIComponent(status)
                             + '&type=' + encodeURIComponent(type)
                             + '&loc=' + encodeURIComponent(loc)
                             + '&areas=' + encodeURIComponent(areas)
                             + '&categories=' + encodeURIComponent(categories)
                             + '&charts=' + encodeURIComponent(charts)
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
            },

            published: function(sortBy, sortOrder, success, error) {
                $http.get(
                        '/rest/messages/published?lang=' + $rootScope.language
                        + '&sortBy=' + sortBy
                        + '&sortOrder=' + sortOrder
                    )
                    .success(success)
                    .error(error);
            },

            getArea: function(areaId, success, error) {
                $http.get('/rest/admin/areas/area/' + areaId)
                    .success(success)
                    .error(error);
            }

        };
    }]);
