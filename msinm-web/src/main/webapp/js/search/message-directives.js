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
    .directive('msiDetailsMenu', ['$rootScope', '$window', function ($rootScope, $window) {
        'use strict';

        return {
            restrict: 'E',
            templateUrl: '/partials/search/details-menu.html',
            scope: {
                messageId: "=",
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
                    $window.location = '/search.html#/search/edit/editor/' + scope.messageId;
                };

                // Navigate to the message manager page
                scope.manage = function() {
                    if (scope.dismissAction) {
                        scope.dismissAction();
                    }
                    $window.location = '/search.html#/search/edit/manage/' + scope.messageId;
                };


            }
        };
    }]);
