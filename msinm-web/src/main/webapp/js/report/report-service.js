
/**
 * Services that provides admin access to the backend
 */
angular.module('msinm.report')

    .factory('ReportService', [ '$http', function($http) {
        'use strict';

        return {

            newReportTemplate: function(success, error) {
                $http.get('/rest/reports/new-report-template')
                    .success(success)
                    .error(error);
            },

            submitReport: function(report, success, error) {
                $http.post('/rest/reports/report', report)
                    .success(success)
                    .error(error);
            },

            updateReportStatus: function(report, success, error) {
                $http.put('/rest/reports/report', report)
                    .success(success)
                    .error(error);
            },

            listFiles: function(dir, success, error) {
                $http.get('/rest/repo/list/' + dir)
                    .success(success)
                    .error(error);
            },

            getPendingReports: function(lang, success, error) {
                $http.get('/rest/reports/pending-reports?lang=' + lang)
                    .success(success)
                    .error(error);
            }

        };
    }]);
