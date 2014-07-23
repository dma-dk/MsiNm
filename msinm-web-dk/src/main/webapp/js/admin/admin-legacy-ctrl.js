
/**
 * The main controllers for the admin app.
 */
angular.module('msinm.admin')

    .factory('LegacyService', [ '$http', function($http) {
        'use strict';

        return {

            getMsiImportStatus: function(success, error) {
                $http.get('/rest/import/legacy-msi/import-status')
                    .success(success)
                    .error(error);
            },

            setMsiImportStatus: function(status, success, error) {
                $http.put('/rest/import/legacy-msi/import-status', status)
                    .success(success)
                    .error(error);
            }
        };
    }])


    /**
     * Legacy Controller
     */
    .controller('LegacyCtrl', ['$scope', '$location', '$modal', 'LegacyService',
        function ($scope, $location, $modal, LegacyService) {
            'use strict';

            $scope.msiImportStatus = { active: false, startDate: undefined, lastUpdate: undefined  };


            function setStatus(data) {
                $scope.msiImportStatus = { active: false, startDate: undefined, lastUpdate: undefined  };
                if (data) {
                    $scope.msiImportStatus.active = data.active;
                    $scope.msiImportStatus.startDate = new Date(data.startDate);
                    $scope.msiImportStatus.startDateStr = $scope.msiImportStatus.startDate.ddmmyyyy();
                    $scope.msiImportStatus.lastUpdate = new Date(data.lastUpdate);
                    $scope.msiImportStatus.lastUpdateStr = $scope.msiImportStatus.lastUpdate.ddmmyyyy();
                }
            }

            $scope.loadMsiImportStatus = function() {
                LegacyService.getMsiImportStatus(function(data) {
                        setStatus(data);
                    },
                    function () {
                        console.log("Error getting legacy MSI import status");
                    });
            };

            $scope.updateMsiImportStatus = function() {

                // TODO: Figure out why the hell this does not work!
                // var date = $("#msiStartDate").datepicker("getDate");

                // Assume dd-mm-yyyy
                var dateStr = $("#msiStartDate").val().split("-");
                $scope.msiImportStatus.startDate = new Date(parseInt(dateStr[2]), parseInt(dateStr[1]) - 1, parseInt(dateStr[0]));

                LegacyService.setMsiImportStatus(
                    $scope.msiImportStatus,
                    function(data) {
                        setStatus(data);
                    },
                    function () {
                        console.log("Error setting legacy MSI import status");
                    });
            };

            // Open modal dialog for uploading and importing legacy NM PDF's.
            $scope.legacyNMImportDlg = function() {

                console.log("IMPORTING PDF");
                $scope.modalInstance = $modal.open({
                    templateUrl : "/partials/admin/legacy-nm-import.html",
                    controller: function ($scope) {
                        $scope.pdfFileUploaded = function(result) {
                            $scope.importResult = result;
                            if(!$scope.$$phase) {
                                $scope.$apply();
                            }
                        };
                    }
                });
            };


        }]);



