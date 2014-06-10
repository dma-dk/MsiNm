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
     * Checks that two password fields match up and that the password is strong
     * Based on http://jsfiddle.net/EHJq8/
     */
    .directive('pwCheck', ['$parse', function ($parse) {
        return {
            require: 'ngModel',
            restrict: 'A',
            link: function (scope, elem, attrs, ctrl) {

                //This part does the matching
                scope.$watch(function() {
                    return (ctrl.$pristine && angular.isUndefined(ctrl.$modelValue)) || $parse(attrs.pwCheck)(scope) === ctrl.$modelValue;
                }, function(currentValue) {
                    ctrl.$setValidity('pwmatch', currentValue);
                });

                //This part is supposed to check the strength
                ctrl.$parsers.unshift(function(viewValue) {
                    var pwdValidLength, pwdHasLetter, pwdHasNumber;

                    pwdValidLength = (viewValue && viewValue.length >= 6 ? true : false);
                    pwdHasLetter = (viewValue && /[A-z]/.test(viewValue)) ? true : false;
                    pwdHasNumber = (viewValue && /\d/.test(viewValue)) ? true : false;

                    ctrl.$setValidity('pwvalid', pwdValidLength && pwdHasLetter && pwdHasNumber);

                    return viewValue;
                });
            }
        }
    }]);

















