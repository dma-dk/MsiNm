
/**
 * Common directives.
 */
angular.module('msinm.common')

    .directive('focus', ['$timeout', function($timeout) {
        'use strict';

        return function(scope, element, attrs) {
            scope.$watch(attrs.focus, function(newValue) {
                $timeout(function() {
                    newValue && element.focus();
                }, 100);
            }, true);
        };
    }])

    .directive('dynamic', function ($compile) {
        return {
            restrict: 'A',
            replace: true,
            link: function (scope, ele, attrs) {
                scope.$watch(attrs.dynamic, function(html) {
                    ele.html(html);
                    $compile(ele.contents())(scope);
                });
            }
        };
    });

