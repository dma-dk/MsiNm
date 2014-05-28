
/**
 * Services that handles user information via the backend
 */
angular.module('msinm.user')

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
     * Holds the currently logged in user
     */
    .factory('Auth', ['$rootScope', '$window', function ($rootScope, $window) {
        'use strict';

        var storage =  $window.sessionStorage;
        //var storage =  $window.localStorage;

        return {
            init: function() {
                if (storage.jwt) {
                    $rootScope.currentUser = JSON.parse(storage.jwt);
                } else {
                    return undefined;
                }
            },

            login: function(jwtToken) {
                storage.clear();
                $rootScope.currentUser = jwtToken;
                if (jwtToken) {
                    storage.jwt = JSON.stringify(jwtToken);
                }
            },

            reauthenticate: function(jwtToken) {
                $rootScope.currentUser.token = jwtToken;
                storage.jwt = JSON.stringify($rootScope.currentUser);
            },

            logout: function() {
                storage.clear();
                delete $rootScope.currentUser;
            },

            isLoggedIn: function() {
                return ($rootScope.currentUser != undefined);
            },

            hasRole: function(role) {
                return $rootScope.currentUser && $rootScope.currentUser.indexOf(role) > -1;
            }

        }
    }])

    .run(['Auth', function (Auth) {
        Auth.init();
    }])

    /**
     * Interface for calling the application server
     */
    .factory('UserService', [ '$http', '$location', 'Auth', function($http, $location, Auth) {
        'use strict';

        return {

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
