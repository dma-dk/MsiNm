
/**
 * Services that retrieves MsiNm messages from the backend
 */
angular.module('msinm')

    /**
     * Interceptor that adds a JWT token to the requests as an authorization header.
     */
    .factory('authInterceptor', ['$rootScope', '$q', '$injector', 'Auth', function ($rootScope, $q, $injector, Auth) {
        'use strict';

        return {
            request: function (config) {
                config.headers = config.headers || {};
                // If the user is logged using a JWT token, add it as a header
                if ($rootScope.currentUser) {
                    config.headers.Authorization = 'Bearer ' + $rootScope.currentUser.token;
                }
                return config;
            },

            response: function (response) {
                if (response.status == 200 && response.headers('Reauthorization')) {
                    Auth.reauthenticate(response.headers('Reauthorization'));
                }
                return response;
            },

            responseError: function(response) {
                switch (response.status) {
                    case 401:
                    case 419:
                        Auth.logout();
                        $rootScope.$broadcast('Login', "Authorization error");
                }
                return $q.reject(response);
            }
        };
    }])

    /**
     * Interface for calling the application server
     */
    .factory('MsiNmService', [ '$http', '$location', 'Auth', function($http, $location, Auth) {
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
            },

            authenticate: function(user, success, error) {
                $http
                    .post('/auth', user, { ignoreAuthModule : true })
                    .success(function (data, status, headers, config) {
                        // Save the JWT token
                        Auth.login(data);
                        success(data);
                    })
                    .error(function (data, status, headers, config) {
                        // Erase the token if the user fails to log in
                        error(data, status);
                    });
            }
        };
    }]);
