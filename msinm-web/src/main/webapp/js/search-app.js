/**
 * The main MsiNM search app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.conf', []);
angular.module('msinm.common', []);
angular.module('msinm.search', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm', [
    'ngRoute', 'ngSanitize', 'ngCookies', 'ui.bootstrap', 'ui.tinymce', 'angularFileUpload', 'pascalprecht.translate', 'growlNotifications',
    'msinm.common', 'msinm.search', 'msinm.conf', 'msinm.map', 'msinm.user'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/search/grid', {
            templateUrl: 'partials/search/search-result-grid.html'
        }).when('/search/map', {
            templateUrl: 'partials/search/search-result-map.html'
        }).when('/search/details', {
            templateUrl: 'partials/search/search-result-details.html'
        }).when('/search/table', {
            templateUrl: 'partials/search/search-result-table.html'
        }).when('/search/edit/editor/:messageId', {
            templateUrl: 'partials/search/message-editor.html',
            resolve: checkRole('editor')
        }).when('/search/edit/copy/:messageId/:reference', {
            templateUrl: 'partials/search/message-editor.html',
            resolve: checkRole('editor')
        }).when('/search/edit/manage/:messageId', {
            templateUrl: 'partials/search/message-manager.html',
            resolve: checkRole('editor')
        }).otherwise({
            redirectTo: '/search/grid'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

