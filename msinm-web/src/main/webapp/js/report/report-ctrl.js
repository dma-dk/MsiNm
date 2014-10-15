
/**
 * The report controller
 */
angular.module('msinm.report')
    .controller('ReportCtrl', ['$scope', '$rootScope', '$routeParams', '$modal', 'growlNotifications', 'ReportService',
        function ($scope, $rootScope, $routeParams, $modal, growlNotifications, ReportService) {
        'use strict';

        $scope.languages = [ $rootScope.language ];
        $scope.report = { description: '', areaId: '', locations: [], repoPath: undefined };
        $scope.attachments = [];
        $scope.uploadUri = '/rest/repo/upload-temp/';
        $scope.pendingReports = [];
        $scope.reportSubmitted = false;

        $scope.init = function() {

            ReportService.newReportTemplate(
                function (data) {
                    $scope.report = data;
                    $scope.uploadUri = '/rest/repo/upload-temp/' + $scope.report.repoPath;
                    $("#messageArea").select2('data', null);
                    $scope.listFiles();
                },
                function (data) {
                    console.error("Error getting new temp dir" + data);
                });
        };

        $scope.fetchPendingReports = function() {
            ReportService.getPendingReports(
                $rootScope.language,
                function (data) {
                    $scope.pendingReports = data;
                },
                function (data) {
                    console.error("Error fetching pending reports " + data);
                });
        };

        $scope.listFiles = function() {
            ReportService.listFiles(
                $scope.report.repoPath,
                function (data) {
                    $scope.attachments = data;
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function (data) {
                    console.error("Error listing files in " + $scope.report.repoPath);
                });
        };

        $scope.submitReport = function() {
            $scope.reportSubmitted = true;
            growlNotifications.add('Sending Report...', 'info', 3000);
            ReportService.submitReport(
                $scope.report,
                function (data) {
                    $scope.init();
                    $rootScope.go('/report/intro');
                },
                function (data) {
                    $scope.reportSubmitted = false;
                    growlNotifications.add('<h4>Report failed</h4><p>' + data + '</p>', 'danger', 3000);
                    console.error("Error listing files in " + $scope.report.repoPath);
                });
        };

        $scope.discardReport = function(report) {
            report.status = 'PROCESSED';
            ReportService.updateReportStatus(
                report,
                function (data) {
                    $scope.fetchPendingReports();
                },
                function (data) {
                    growlNotifications.add('<h4>Update failed</h4><p>' + data + '</p>', 'danger', 5000);
                    console.error("Error discarding report " + report);
                });
        };

        $scope.attachmentUploaded = function(result) {
            $scope.listFiles();
            if(!$scope.$$phase) {
                $scope.$apply();
            }
        };

        // Callback, called when an attachment has been deleted
        $scope.attachmentDeleted = function(result) {
            $scope.listFiles();
            if(!$scope.$$phase) {
                $scope.$apply();
            }
        };

    }]);

