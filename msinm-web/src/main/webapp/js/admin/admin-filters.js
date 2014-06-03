/**
 * Admin-related angular filters
 */
angular.module('msinm.admin')

    .filter('serialize', function () {
        return function (input) {
            input = input || [];
            return input.join();
        };
    });
