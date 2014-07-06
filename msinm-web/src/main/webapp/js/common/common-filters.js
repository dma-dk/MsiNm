
/**
 * Common angular filters
 */
angular.module('msinm.common')

    .filter('serialize', function () {
        return function (input) {
            input = input || [];
            return input.join();
        };
    })

    .filter('plain2html', function () {
        return function (text) {
            return ((text || "") + "")  // make sure it's a string;
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\t/g, "    ")
                .replace(/ /g, "&#8203;&nbsp;&#8203;")
                .replace(/\r\n|\r|\n/g, "<br />");
        };
    });

