
/**
 * The home controller
 */
angular.module('msinm.search')
    .controller('HomeCtrl', ['$scope', 'SearchService',
        function ($scope, SearchService) {
            'use strict';

            $scope.searchResult = { messages: [], startIndex: 0, total: 0 };

            $scope.search = function () {
                SearchService.search(
                    '', // query
                    'ACTIVE',
                    '', // type
                    '', // location
                    '', // from date
                    '', // to date
                    100,
                    0,
                    'DATE',
                    'DESC',
                    function(data) {
                        $scope.searchResult = data;
                    },
                    function () {
                        //alert("Error");
                    }
                );
            };

        }]);