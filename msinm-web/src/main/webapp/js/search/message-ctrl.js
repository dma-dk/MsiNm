/**
 * The message search controller for the app.
 */
angular.module('msinm.search')
    .controller('MessageCtrl', ['$scope', '$modal', 'growlNotifications', 'MessageService', 'messageId',
        function ($scope, $modal, growlNotifications, MessageService, messageId) {
            'use strict';

            $scope.messageId = messageId;
            $scope.msg = undefined;

            $("body").addClass("no-print");

            $scope.$on("$destroy", function() {
                $("body").removeClass("no-print");
            });


            MessageService.details(
                messageId,
                function (data) {
                    $scope.msg = data;
                },
                function (data) {
                    growlNotifications.add('<h4>Message Lookup Failed</h4>', 'danger', 3000);
                });


        }]);