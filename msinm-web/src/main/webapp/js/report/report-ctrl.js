
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
            ReportService.submitReport(
                $scope.report,
                function (data) {
                    $scope.init();
                    $rootScope.go('/report/intro');
                },
                function (data) {
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

        $scope.tinymceOptions = {
            resize: false,
            plugins: [
                "autolink lists link image anchor",
                "code textcolor",
                "media table contextmenu paste"
            ],
            theme: "modern",
            skin: 'light',
            statusbar : false,
            menubar: false,
            contextmenu: "link image inserttable | cell row column deletetable",
            toolbar: "styleselect | bold italic | forecolor backcolor | alignleft aligncenter alignright alignjustify | "
                    + "bullist numlist  | outdent indent | link image table | code",

            file_browser_callback: function(field_name, url, type, win) {
                $(".mce-window").hide();
                $("#mce-modal-block").hide();
                var scope = angular.element($("#tinymce")).scope();
                scope.$apply(function() {
                    var modalInstance = scope.open();
                    modalInstance.result.then(function (file) {
                        $("#mce-modal-block").show();
                        $(".mce-window").show();
                        win.document.getElementById(field_name).value = "/rest/repo/file/" + file.path;
                    }, function () {
                        console.log('Modal dismissed at: ' + new Date());
                    });
                });
            }
        };

        $scope.open = function (size) {
            var modalInstance = $modal.open({
                templateUrl: 'myModalContent.html',
                controller: ModalInstanceCtrl,
                size: size,
                windowClass: 'on-top',
                resolve: {
                    files: function () {
                        return $scope.attachments;
                    }
                }
            });
            return modalInstance;
        }


    }]);

var ModalInstanceCtrl = function ($scope, $modalInstance, files) {
    $scope.files = files;

    $scope.ok = function (file) {
        $modalInstance.close(file);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};
