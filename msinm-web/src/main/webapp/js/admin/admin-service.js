
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.admin')


    .factory('LegacyService', [ '$http', '$location', function($http, $location) {
        'use strict';

        var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();

        return {

            importMsiNm: function(success, error) {
                $http.get(host + '/rest/import/legacy-ws-msi')
                    .success(success)
                    .error(error);
            },

            importLegacyMsi: function(count, success, error) {
                $http.get(host + '/rest/import/legacy-db-msi?limit=' + count)
                    .success(success)
                    .error(error);
            }
        };
    }])

    .factory('AdminUserService', [ '$http', '$location', function($http, $location) {
        'use strict';

        var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();

        return {

            listUsers: function(success, error) {
                $http.get(host + '/rest/user/all')
                    .success(success)
                    .error(error);
            }
        };
    }])

    .factory('OperationsService', [ '$http', '$location', function($http, $location) {
        'use strict';

        var host = $location.protocol() + '://' + $location.host() + ':' + $location.port();

        return {

            recreateSearchIndex: function(success, error) {
                $http.get(host + '/rest/message/recreate-search-index')
                    .success(success)
                    .error(error);
            },

            clearCaches: function(cacheId, success, error) {
                $http.get(host + '/rest/admin/operations/reset-cache/' + cacheId)
                    .success(success)
                    .error(error);
            }

        };
    }])

    .factory('AreaService', [ '$http', '$location', function($http, $location) {
        'use strict';

        return {
            getAreas: function(success, error) {
                $http.get('/rest/message/area-roots')
                    .success(success)
                    .error(error);
            },

            createArea: function(area, success, error) {
                $http.post('/rest/message/area', area)
                    .success(success)
                    .error(error);
            },

            updateArea: function(area, success, error) {
                $http.put('/rest/message/area', area)
                    .success(success)
                    .error(error);
            }
        };
    }]);

