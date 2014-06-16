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
        'use strict';

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
    }])


    /**
     * File upload, based on:
     * https://github.com/nervgh/angular-file-upload
     * <p>
     * The directive takes the following attributes:
     * <ul>
     *   <li>repo-folder: The folder wihtin the repo. Mandatory.</li>
     *   <li>multiple: Support single or multiple file upload. Defaults to false.</li>
     *   <li>auto-upload: Automatically start upload. Defaults to false.</li>
     *   <li>remove-after-upload: Remove file from queue once uploaded. Defaults to false.</li>
     *   <li>success(result): Success callback function. Optional.</li>
     *   <li>error(status, statusText): Error callback function. Optional.</li>
     * </ul>
     */
    .directive('msiFileUpload', ['$fileUploader', 'Auth', function ($fileUploader, Auth) {
        'use strict';

        return {
            restrict: 'AE',

            templateUrl: '/partials/common/file-upload.html',

            scope: {
                repoFolder:         '=repoFolder',
                multiple:           '=multiple',
                dropText:           '@dropText',
                autoUpload:         '=autoUpload',
                removeAfterUpload:  '=removeAfterUpload',
                success:            '&success',
                error:              '&error'
            },

            compile: function(element, attrs) {
                if (attrs.dropText == undefined) {
                    attrs.$set("dropText", (attrs.multiple) ? 'or drop files here' : 'or drop file here');
                }

                return function (scope, element, attrs) {
                    // create a uploader with options
                    var uploader = scope.uploader = $fileUploader.create({
                        scope: scope,
                        url: scope.repoFolder
                    });

                    // Auto-upload
                    if (scope.autoUpload) {
                        uploader.autoUpload = scope.autoUpload;
                    }

                    // Remove after upload
                    if (scope.removeAfterUpload) {
                        uploader.removeAfterUpload = scope.removeAfterUpload;
                    }

                    // Handle authenticaiton
                    if (Auth.isLoggedIn()) {
                        uploader.headers.Authorization = Auth.authorizationHeader();
                    }

                    scope.cancelOrRemove = function(item) {
                        if (item.isUploading) {
                            item.cancel();
                        } else {
                            item.remove();
                        }
                    };

                    scope.$watch(attrs.repoFolder, function (value) {
                        uploader.url = value;
                    }, true);

                    // Success call-back
                    if (scope.success) {
                        uploader.bind('success', function (event, xhr, item, response) {
                            scope.success({ result: response});
                        });
                    }

                    // Error call-back
                    if (scope.error) {
                        uploader.bind('error', function (event, xhr, item, response) {
                            scope.error({ status: xhr.status, statusText: xhr.statusText });
                        });
                    }
                }

            }

        }
    }]);



















