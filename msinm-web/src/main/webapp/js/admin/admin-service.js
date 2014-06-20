
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.admin')


    .factory('LegacyService', [ '$http', function($http) {
        'use strict';

        return {

            importMsiNm: function(success, error) {
                $http.get('/rest/import/legacy-ws-msi')
                    .success(success)
                    .error(error);
            },

            importLegacyMsi: function(count, success, error) {
                $http.get('/rest/import/legacy-db-msi?limit=' + count)
                    .success(success)
                    .error(error);
            }
        };
    }])

    .factory('AdminUserService', [ '$http', function($http) {
        'use strict';

        return {

            listUsers: function(success, error) {
                $http.get('/rest/user/all')
                    .success(success)
                    .error(error);
            }
        };
    }])

    .factory('OperationsService', [ '$http', function($http) {
        'use strict';

        return {

            recreateSearchIndex: function(success, error) {
                $http.get('/rest/message/recreate-search-index')
                    .success(success)
                    .error(error);
            },

            clearCaches: function(cacheId, success, error) {
                $http.get('/rest/admin/operations/reset-cache/' + cacheId)
                    .success(success)
                    .error(error);
            }

        };
    }])

    .factory('AreaService', [ '$http', function($http) {
        'use strict';

        return {
            getAreas: function(success, error) {
                $http.get('/rest/admin/areas/area-roots')
                    .success(success)
                    .error(error);
            },

            createArea: function(area, success, error) {
                $http.post('/rest/admin/areas/area', area)
                    .success(success)
                    .error(error);
            },

            updateArea: function(area, success, error) {
                $http.put('/rest/admin/areas/area', area)
                    .success(success)
                    .error(error);
            },

            deleteArea: function(area, success, error) {
                $http.delete('/rest/admin/areas/area/' + area.id)
                    .success(success)
                    .error(error);
            },

            moveArea: function(areaId, parentId, success, error) {
                $http.put('/rest/admin/areas/move-area', { areaId: areaId, parentId: parentId })
                    .success(success)
                    .error(error);
            }
        };
    }])


    .factory('ChartService', [ '$http', function($http) {
        'use strict';

        return {
            getCharts: function(success, error) {
                $http.get('/rest/admin/charts/all')
                    .success(success)
                    .error(error);
            },

            createChart: function(chart, success, error) {
                $http.post('/rest/admin/charts/chart', chart)
                    .success(success)
                    .error(error);
            },

            updateChart: function(chart, success, error) {
                $http.put('/rest/admin/charts/chart', chart)
                    .success(success)
                    .error(error);
            },

            deleteChart: function(chart, success, error) {
                $http.delete('/rest/admin/charts/chart/' + chart.id)
                    .success(success)
                    .error(error);
            }
        };
    }]);
