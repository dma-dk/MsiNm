
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', function ($rootScope) {

        $rootScope.LEGACY_ADMIN_PAGE = false;

    }]);
