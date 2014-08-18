/**
 * The message search controller for the app.
 */
angular.module('msinm.search')

    /**
     * Controller that handles editing messages
     */
    .controller('MessageEditorCtrl', ['$scope', '$rootScope', '$routeParams', '$modal', 'growlNotifications', 'MessageService',
        function ($scope, $rootScope, $routeParams, $modal, growlNotifications, MessageService) {
            'use strict';

            $scope.msg = { descs: [], locations: [], areadId: undefined };
            $scope.locationsLoaded = false;
            $scope.messageSaved = false;

            $scope.copyAreaLocations = function() {
                if ($scope.msg.areaId) {
                    $scope.locationsLoaded = false;
                    MessageService.getArea(
                        $scope.msg.areaId,
                        function (data) {
                            $scope.locationsLoaded = true;
                            $scope.msg.locations = data.locations ? data.locations : [];
                        },
                        function (data) {
                            growlNotifications.add('<h4>Area Lookup Failed</h4>', 'danger', 3000);
                        }
                    )
                }
            };

            // Load the message details for the given message id
            $scope.loadMessageDetails = function() {
                if ($routeParams.messageId != 'new') {
                    MessageService.details(
                        $routeParams.messageId,
                        function (data) {
                            $scope.msg = data;
                            if (data.area) {
                                data.areaId = data.area.id;
                                $("#editorArea").select2("data", {id: data.area.id, text: data.area.descs[0].name });
                            }
                            $scope.locationsLoaded = true;
                            $scope.messageSaved = false;
                            $scope.areaForm.$setPristine();
                        },
                        function (data) {
                            growlNotifications.add('<h4>Message Lookup Failed</h4>', 'danger', 3000);
                        });
                }
            };

            $scope.loadMessageDetails();

            $scope.saveMessage = function () {
                $scope.messageSaved = true;
                // TODO
            }

            $scope.reloadMessage = function () {
                $scope.loadMessageDetails();
            }

        }])


    /**
     * Controller that handles displaying message details
     */
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


    /**
     * Controller that handles displaying message details in a dialog
     */
    .controller('MessageDialogCtrl', ['$scope', '$window', 'growlNotifications', 'MessageService', 'messageId', 'messages',
        function ($scope, $window, growlNotifications, MessageService, messageId, messages) {
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

            $scope.pdf = function () {
                var messageId = $scope.pushedMessageIds[$scope.pushedMessageIds.length - 1];
                $window.location = '/rest/messages/message-pdf/' + messageId + '.pdf?lang=' + $scope.language;
            };

            $scope.calendar = function () {
                var messageId = $scope.pushedMessageIds[$scope.pushedMessageIds.length - 1];
                $window.location = '/rest/messages/message-cal/' + messageId + '.ics?lang=' + $scope.language;
            };

            $scope.edit = function() {
                $scope.$dismiss('edit');
                //$scope.go('/search/edit/' + $scope.msg.id);
                $window.location = '/search.html#/search/edit/' + $scope.msg.id;
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