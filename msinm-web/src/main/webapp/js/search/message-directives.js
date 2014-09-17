/**
 * Common directives.
 */
angular.module('msinm.search')


    /****************************************************************
     * Binds a click event that will open the message details dialog
     ****************************************************************/
    .directive('msiMessageDetails', ['$rootScope',
        function ($rootScope) {
        'use strict';

        return {
            restrict: 'A',
            scope: {
                msiMessageDetails: "=",
                msiMessages: "="
            },
            link: function(scope, element, attrs) {

                element.bind('click', function() {
                    $rootScope.$broadcast('messageDetails', {
                        messageId: scope.msiMessageDetails,
                        messages: scope.msiMessages
                    });
                });
            }
        };
    }])


    /********************************
     * Renders the message details
     ********************************/
    .directive('msiRenderMessageDetails', [ '$rootScope', function ($rootScope) {
        'use strict';

        return {
            restrict: 'A',
            templateUrl: '/partials/search/render-message-details.html',
            replace: false,
            scope: {
                msg: "=",
                messages: "=",
                showDetailsMenu: "@",
                showPublications: "@"
            },
            link: function(scope, element, attrs) {
                scope.language = $rootScope.language;
            }
        };
    }])


    /****************************************************************
     * Adds a message details drop-down menu
     ****************************************************************/
    .directive('msiDetailsMenu', ['$rootScope', '$window', 'growlNotifications', 'MessageService',
        function ($rootScope, $window, growlNotifications, MessageService) {
        'use strict';

        return {
            restrict: 'E',
            templateUrl: '/partials/search/details-menu.html',
            scope: {
                messageId: "=",     // NB: We supply both of "messageId" and "msg" because
                msg: "=",           // the former may be invalid and the latter may be undefined.
                messages: "=",
                style: "@",
                size: "@",
                showViewAction: "@",
                dismissAction: "&"
            },
            link: function(scope, element, attrs) {

                if (scope.style) {
                    element.attr('style', scope.style);
                }

                if (scope.size) {
                    $(element[0]).find('button').addClass("btn-" + scope.size);
                }

                scope.hasRole = $rootScope.hasRole;

                // Open the details viewer
                scope.view = function () {
                    $rootScope.$broadcast('messageDetails', {
                        messageId: scope.messageId,
                        messages: scope.messages
                    });
                };

                // Download the PDF for the message
                scope.pdf = function () {
                    $window.location = '/rest/messages/message-pdf/' + scope.messageId + '.pdf?lang=' + $rootScope.language;
                };

                // Download the calendar for the message
                scope.calendar = function () {
                    $window.location = '/rest/messages/message-cal/' + scope.messageId + '.ics?lang=' + $rootScope.language;
                };

                // Navigate to the message editor page
                scope.edit = function() {
                    if (scope.dismissAction) {
                        scope.dismissAction();
                    }
                };

                // Navigate to the message manager page
                scope.manage = function() {
                    if (scope.dismissAction) {
                        scope.dismissAction();
                    }
                };

                scope.addBookmark = function () {
                    if (scope.msg && !scope.msg.bookmarked) {
                        MessageService.addBookmark(
                            scope.messageId,
                            function (data) {
                                scope.msg.bookmarked = true;
                            },
                            function (data) {
                                growlNotifications.add('<h4>Failed adding bookmark</h4>', 'danger', 3000);
                            });
                    }
                };

                scope.removeBookmark = function () {
                    if (scope.msg && scope.msg.bookmarked) {
                        MessageService.removeBookmark(
                            scope.messageId,
                            function (data) {
                                scope.msg.bookmarked = false;
                            },
                            function (data) {
                                growlNotifications.add('<h4>Failed removing bookmark</h4>', 'danger', 3000);
                            });
                    }
                };

            }
        }
    }])

    /********************************
     * Renders the JSON diff structure
     ********************************/
    .directive('msiJsonDiff', [ '$document', function ($document) {
        'use strict';

        return {
            restrict: 'A',
            scope: {
                history: "="
            },
            link: function(scope, element, attrs) {

                $document.on('click', function (e) {
                    e = e || window.event;
                    if (e.target.nodeName.toUpperCase() === "UL") {
                        if (e.target.getAttribute("closed") === "yes") {
                            e.target.setAttribute("closed", "no");
                        } else {
                            e.target.setAttribute("closed", "yes");
                        }
                    }
                });

                scope.$watchCollection(function () {
                        return scope.history;
                    },
                    function (newValue) {
                        element.empty();

                        if (scope.history.length > 0) {

                            try {
                                var hist1 = JSON.parse(scope.history[0].snapshot);
                                var hist2 = (scope.history.length > 1) ? JSON.parse(scope.history[1].snapshot) : hist1;
                                jsond.compare(hist1, hist2, "Message", element[0]);
                            } catch (e) {
                                console.error("Error " + e);
                            }
                        }

                    });
            }
        };
    }])


    /******************************************
     * Directive that wraps the datetimepicker
     * http://eonasdan.github.io/bootstrap-datetimepicker/
     *****************************************/
    .directive('msiDateTimePicker', [ '$rootScope', function($rootScope) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                date: '=',
                format: '@',
                placeholder: '@'
            },
            template: "<div class='input-group date'>\n  <input type='text' class='form-control input-sm'/>\n  <span class='input-group-addon'>\n    <span class='fa fa-calendar'></span>\n  </span>\n</div>",
            link: function(scope, element, attr) {

                var format = "DD-MM-YYYY HH:mm";
                if (attr.format) {
                    format = attr.format;
                }

                var input = element.find("input");
                input.attr('data-date-format', format);

                if (attr.placeholder) {
                    input.attr('placeholder', attr.placeholder);
                }

                var picker = element.datetimepicker({
                    pickDate: true,
                    pickTime: true,
                    useMinutes: true,
                    useSeconds: false,
                    useCurrent: false,
                    showToday: true,
                    language: 'en',
                    defaultDate: "",
                    pick12HourFormat: false,
                    icons: {
                        time: 'fa fa-clock-o',
                        date: 'fa fa-calendar',
                        up: 'fa fa-arrow-up',
                        down: 'fa fa-arrow-down'
                    }}).data("DateTimePicker");

                element.find('.input-group-addon').on('click', function(e) {
                    return input.focus();
                });

                // Listen for date picker changed
                element.on("change.dp", function(e) {
                    if (picker.date && !picker.unset && picker.date.valueOf() != scope.date) {
                        scope.date = picker.date.valueOf();
                        $rootScope.$$phase || $rootScope.$apply();
                    }
                });

                // Watch for date model changes
                scope.$watch(function () {
                    return scope.date;
                }, function(newValue, oldValue) {
                    picker.setDate(newValue ? new Date(newValue) : undefined);
                    input.val(newValue ? moment(newValue).format(format) : '');
                }, true);

                // The datetimepicker does not pick up when the input field is cleared
                input.bind('blur', function() {
                    if (!input.val() && scope.date) {
                        scope.date = undefined;
                        $rootScope.$$phase || $rootScope.$apply();
                    }
                });
            }
        };
    }]);

