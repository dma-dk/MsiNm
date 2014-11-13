
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.admin')


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
                $http.get('/rest/messages/recreate-search-index')
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
    }])


    .factory('AreaService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        return {
            getAreas: function(success, error) {
                $http.get('/rest/admin/areas/area-roots?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            getArea: function(area, success, error) {
                $http.get('/rest/admin/areas/area/' + area.id)
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
            },

            changeSortOrder: function(areaId, moveUp, success, error) {
                $http.put('/rest/admin/areas/change-sort-order', { areaId: areaId, moveUp: moveUp })
                    .success(success)
                    .error(error);
            },

            recomputeTreeSortOrder: function(success, error) {
                $http.get('/rest/admin/areas/recompute-tree-sort-order')
                    .success(success)
                    .error(error);
            }

        };
    }])


    .factory('CategoryService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        return {
            getCategories: function(success, error) {
                $http.get('/rest/admin/categories/category-roots?lang=' + $rootScope.language)
                    .success(success)
                    .error(error);
            },

            getCategory: function(category, success, error) {
                $http.get('/rest/admin/categories/category/' + category.id)
                    .success(success)
                    .error(error);
            },

            createCategory: function(category, success, error) {
                $http.post('/rest/admin/categories/category', category)
                    .success(success)
                    .error(error);
            },

            updateCategory: function(category, success, error) {
                $http.put('/rest/admin/categories/category', category)
                    .success(success)
                    .error(error);
            },

            deleteCategory: function(category, success, error) {
                $http.delete('/rest/admin/categories/category/' + category.id)
                    .success(success)
                    .error(error);
            },

            moveCategory: function(categoryId, parentId, success, error) {
                $http.put('/rest/admin/categories/move-category', { categoryId: categoryId, parentId: parentId })
                    .success(success)
                    .error(error);
            }
        };
    }])

    .factory('SettingsService', [ '$http', function($http) {
        'use strict';

        return {
            getSettings: function(success, error) {
                $http.get('/rest/admin/settings/all')
                    .success(success)
                    .error(error);
            },

            getSetting: function(key, success, error) {
                $http.get('/rest/admin/settings/setting/' + key)
                    .success(success)
                    .error(error);
            },

            updateSetting: function(setting, success, error) {
                $http.put('/rest/admin/settings/setting', setting)
                    .success(success)
                    .error(error);
            }
        };
    }])

    .factory('PublisherService', [ '$http', function($http) {
        'use strict';

        return {

            getPublishers: function(success, error) {
                $http.get('/rest/messages/publishers')
                    .success(success)
                    .error(error);
            },

            updatePublisher: function(publisher, success, error) {
                $http.put('/rest/messages/publisher', publisher)
                    .success(success)
                    .error(error);
            }
        };
    }]);



