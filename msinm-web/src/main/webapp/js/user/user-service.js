
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
                    config.headers.Authorization = Auth.authorizationHeader();
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
    .factory('Auth', ['$rootScope', '$window',
        function ($rootScope, $window) {
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

            authorizationHeader: function() {
                if ($rootScope.currentUser) {
                    return 'Bearer ' + $rootScope.currentUser.token;
                }
                return '';
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
                return $rootScope.currentUser && $rootScope.currentUser.roles.indexOf(role) > -1;
            }
        }
    }])

    .run(['$rootScope', '$location', '$modal', 'Auth', function ($rootScope, $location, $modal, Auth) {
        Auth.init();

        $rootScope.$on('Login', function (event, message) {
            if (!$rootScope.loginDialog) {
                $rootScope.loginDlg();
                $rootScope.message = message;
            }
        });

        $rootScope.loginDlg = function() {
            // Require IE 11 or newer
            if (isIE() < 11) {
                alert("For this prototype, please use IE version 11 or newer.\nOr use a new'ish version of Chrome, Firefox or Safari");
                return;
            }

            $rootScope.loginDialog = $modal.open({
                controller: "UserCtrl",
                templateUrl : "/partials/user/login-dialog.html",
                size: 'sm'
            });
            return $rootScope.loginDialog;
        };

        $rootScope.logout = function () {
            Auth.logout();
        };

        $rootScope.isLoggedIn = function() {
            return Auth.isLoggedIn();
        };

        $rootScope.hasRole = function(role) {
            return Auth.hasRole(role);
        };

        $rootScope.go = function ( path ) {
            $location.path( path );
        };
    }])

    /**
     * Interface for calling the application server with User related operations
     */
    .factory('UserService', [ '$http', '$location', 'Auth', function($http, $location, Auth) {
        'use strict';

        return {

            authenticate: function(payload, success, error) {
                $http
                    .post('/auth', payload)
                    .success(function (data, status, headers, config) {
                        // Save the JWT token
                        Auth.login(data);
                        success(data);
                    })
                    .error(function (data, status, headers, config) {
                        // Erase the token if the user fails to log in
                        error(data, status);
                    });
            },

            resetPassword: function(email, success, error) {
                $http
                    .post('/rest/user/reset-password', email)
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            updatePassword: function(email, password, token, success, error) {
                $http
                    .post('/rest/user/update-password', { email: email, password: password, token: token })
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            currentUser: function(success, error) {
                $http.get('/rest/user/current-user')
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            updateCurrentUser: function(email, firstName, lastName, language, mmsi, vesselName, success, error) {
                $http
                    .put('/rest/user/current-user', { email: email, firstName: firstName, lastName: lastName, language: language, mmsi: mmsi, vesselName: vesselName })
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            registerUser: function(email, firstName, lastName, language, mmsi, vesselName, success, error) {
                $http
                    .post('/rest/user/register-user', { email: email, firstName: firstName, lastName: lastName, language: language, mmsi: mmsi, vesselName: vesselName })
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            createOrUpdateUser: function(email, firstName, lastName, language, roles, activationEmail, success, error) {
                $http
                    .post('/rest/user/create-or-update-user', {
                        email: email,
                        firstName: firstName,
                        lastName: lastName,
                        language: language,
                        roles: roles,
                        activationEmail: activationEmail
                    })
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            }
        };
    }])


    /**
     * Interface for calling the application server with Mailing List related operations
     */
    .factory('MailingListService', [ '$http', function($http) {
        'use strict';

        return {

            getUserMailingListTemplates: function(success, error) {
                $http.get('/rest/mailing-lists/user-mailing-list-templates')
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            getAllMailingLists: function(success, error) {
                $http.get('/rest/mailing-lists/all-mailing-lists')
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            getUserMailingLists: function(success, error) {
                $http.get('/rest/mailing-lists/user-mailing-lists')
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            updateUserSubscription: function(mailListIds, success, error) {
                $http.put('/rest/mailing-lists/update-user-subscription', mailListIds )
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            createMailingList: function(mailList, success, error) {
                $http.post('/rest/mailing-lists/mailing-list', mailList)
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            updateMailingList: function(mailList, success, error) {
                $http.put('/rest/mailing-lists/mailing-list', mailList)
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            deleteMailingList: function(mailListId, success, error) {
                $http.delete('/rest/mailing-lists/mailing-list/' + mailListId )
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            },

            newMailingListTemplate: function(params, success, error) {
                $http.get('/rest/mailing-lists/new-mailing-list-template?' + params)
                    .success(function (data) {
                        success(data);
                    })
                    .error(function (data) {
                        error(data);
                    });
            }
        };
    }]);


/**
 * Checks that the user has the given role. Otherwise, redirects to "/"
 * @param role the role to check
 */
checkRole = function (role) {
    return {
        load: function ($q, Auth, $http) {

            function unauthorizedAccess() {
                console.error("User must have role " + role + ". Redirecting to front page");
                location.href = "/";
            }

            // NB: Originally, we just checked using Auth.hasRole(role)
            // However, calling the server will keep the session alive
            // and trap expired sessions.
            if (role) {
                var deferred = $q.defer();
                $http.get("/rest/user/check-role/" + role)
                    .success(function(data) {
                        // The server has returned 'true', 'false' or 'error'
                        if (data == 'true') {
                            deferred.resolve();
                        } else {
                            if (data == 'error') {
                                Auth.logout();
                            }
                            deferred.reject();
                            unauthorizedAccess();
                        }
                    })
                    .error(function(data) {
                        deferred.reject();
                        unauthorizedAccess();
                    });

                return deferred.promise;
            } else {
                unauthorizedAccess();
            }
        }
    }
};

