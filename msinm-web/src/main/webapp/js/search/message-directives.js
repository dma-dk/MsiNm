/**
 * Common directives.
 */
angular.module('msinm.search')


    .directive('msiMessageDetails', ['$modal',
        function ($modal) {
        'use strict';

        return {
            restrict: 'A',
            scope: {
                msiMessageDetails: "="
            },
            link: function(scope, element, attrs) {

                element.bind('click', function() {
                    $modal.open({
                        controller: "MessageCtrl",
                        templateUrl: "/partials/search/message-details.html",
                        size: 'lg',
                        resolve: {
                            messageId: function () {
                                return scope.msiMessageDetails.id;
                            }
                        }
                    });
                });

            }
        };
    }]);
