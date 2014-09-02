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
                showDetailsMenu: "@"
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
    }]);

