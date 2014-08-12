/**
 * The message search controller for the app.
 */
angular.module('msinm.search')

    .controller('MessageDetailsCtrl', ['$scope', '$modal',
        function ($scope, $modal) {
            'use strict';

            function extractMessageIds(messages) {
                var ids = [];
                if (messages) {
                    for (var i in messages) {
                        ids.push(messages[i].id);
                    }
                }
                return ids;
            }

            $scope.$on('messageDetails', function (event, data) {
                $modal.open({
                    controller: "MessageDialogCtrl",
                    templateUrl: "/partials/search/message-details.html",
                    size: 'lg',
                    resolve: {
                        messageId: function () {
                            return data.messageId;
                        },
                        messages: function () {
                            return extractMessageIds(data.messages);
                        }
                    }
                });
            });

        }])

    .controller('MessageDialogCtrl', ['$scope', 'growlNotifications', 'MessageService', 'messageId', 'messages',
        function ($scope, growlNotifications, MessageService, messageId, messages) {
            'use strict';

            $scope.warning = undefined;
            $scope.messages = messages;
            $scope.pushedMessageIds = [];
            $scope.pushedMessageIds[0] = messageId;

            $scope.msg = undefined;
            $scope.index = $.inArray(messageId, messages);
            $scope.showNavigation = $scope.index >= 0;

            // Attempt to improve printing
            $("body").addClass("no-print");
            $scope.$on("$destroy", function() {
                $("body").removeClass("no-print");
            });

            $scope.selectPrev = function() {
                if ($scope.pushedMessageIds.length == 1 && $scope.index > 0) {
                    $scope.index--;
                    $scope.pushedMessageIds[0] = $scope.messages[$scope.index];
                    $scope.loadMessageDetails();
                }
            };

            $scope.selectNext = function() {
                if ($scope.pushedMessageIds.length == 1 && $scope.index >= 0 && $scope.index < $scope.messages.length - 1) {
                    $scope.index++;
                    $scope.pushedMessageIds[0] = $scope.messages[$scope.index];
                    $scope.loadMessageDetails();
                }
            };

            $scope.selectMessage = function (messageId) {
                $scope.pushedMessageIds.push(messageId);
                $scope.loadMessageDetails();
            };

            $scope.back = function () {
                if ($scope.pushedMessageIds.length > 1) {
                    $scope.pushedMessageIds.pop();
                    $scope.loadMessageDetails();
                }
            };

            // Load the message details for the given message id
            $scope.loadMessageDetails = function() {

                MessageService.details(
                    $scope.pushedMessageIds[$scope.pushedMessageIds.length - 1],
                    function (data) {
                        $scope.warning = (data) ? undefined : "Message not found";
                        $scope.msg = data;
                    },
                    function (data) {
                        $scope.msg = undefined;
                        growlNotifications.add('<h4>Message Lookup Failed</h4>', 'danger', 3000);
                    });
            };

            $scope.loadMessageDetails();

        }]);