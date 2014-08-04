
/**
 * The report controller
 */
angular.module('msinm.report')
    .controller('ReportCtrl', ['$scope', '$routeParams', '$modal', 'growlNotifications', 'ReportService',
        function ($scope, $routeParams, $modal, growlNotifications, ReportService) {
        'use strict';

        $scope.report = { description: undefined, areaId: undefined, locations: [], repoDir: undefined };
        $scope.attachments = [];
        $scope.uploadUri = '/rest/repo/upload-temp/';

        initAreaField("#messageArea", false);

        $scope.init = function() {

            ReportService.newReportTemplate(
                function (data) {
                    $scope.report = data;
                    $scope.uploadUri = '/rest/repo/upload-temp/' + $scope.report.repoPath;
                    $scope.listFiles();
                },
                function (data) {
                    console.error("Error getting new temp dir" + data);
                });
        };

        $scope.listFiles = function() {
            ReportService.listFiles(
                $scope.report.repoPath,
                function (data) {
                    $scope.attachments = data;
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
                    growlNotifications.add('<h4>Report Submitted</h4>', 'success', 3000);
                },
                function (data) {
                    growlNotifications.add('<h4>Report failed</h4><p>' + data + '</p>', 'danger', 3000);
                    console.error("Error listing files in " + $scope.report.repoPath);
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
