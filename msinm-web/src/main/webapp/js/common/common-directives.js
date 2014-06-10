/**
 * Common directives.
 */
angular.module('msinm.common')

    .directive('focus', ['$timeout', function ($timeout) {
        'use strict';

        return function (scope, element, attrs) {
            scope.$watch(attrs.focus, function (newValue) {
                $timeout(function () {
                    newValue && element.focus();
                }, 100);
            }, true);
        };
    }])

    .directive('dynamic', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            replace: true,
            link: function (scope, ele, attrs) {
                scope.$watch(attrs.dynamic, function (html) {
                    ele.html(html);
                    $compile(ele.contents())(scope);
                });
            }
        };
    }])

/**
 * Show element active/inactive depending on the current location.
 * Usage:
 * <pre>
 *     <li check-active="/search/*"><a href="search.html">Search</a></li>
 * </pre>
 * <p>
 * Inspired by:
 *   http://stackoverflow.com/questions/16199418/how-do-i-implement-the-bootstrap-navbar-active-class-with-angular-js
 * - but changed quite a bit.
 */
    .directive('checkActive', [ '$location', function ($location) {
        'use strict';

        return {
            restrict: 'A',
            scope: {
                checkActive: "@"
            },
            link: function (scope, element, attrs) {

                // Watch for the $location
                scope.$watch(function () {
                    return $location.path();
                }, function (newValue, oldValue) {

                    var locMask = scope.checkActive.split("*").join(".*");
                    var regexp = new RegExp('^' + locMask + '$', ['i']);

                    if (regexp.test(newValue)) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                });
            }
        };
    }])

    /**
     * Checks that two password fields match up.
     * Based on http://blog.brunoscopelliti.com/angularjs-directive-to-check-that-passwords-match
     */
    .directive('pwCheck', [function () {
        return {
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {
                var firstPassword = '#' + attrs.pwCheck;
                elem.add(firstPassword).on('keyup', function () {
                    scope.$apply(function () {
                        var v = elem.val() === $(firstPassword).val();
                        ctrl.$setValidity('pwmatch', v);
                    });
                });
            }
        }
    }]);

















