
/**
 * Common angular filters
 */
angular.module('msinm.common')

    .filter('serialize', function () {
        return function (input, separator) {
            input = input || [];
            return input.join(separator);
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
    })

    .filter('truncate', function () {
        return function (text, chars) {
            var truncated = false;
            text = (text || "") + "";
            // only first line
            if (text.indexOf('\n') != -1) {
                text = text.substr(0, text.indexOf('\n'));
                truncated = true;
            }

            // Limit chars
            if (chars && text.length > chars) {
                text = text.substr(0, chars);
                truncated = true;
            }

            if (truncated) {
                text = text + "\u2026";
            }
            return text;
        };
    })

    .filter('formatJson', function () {
        return function (text) {
            try {
                var json = JSON.parse(text);
                return JSON.stringify(json, null, 2);
            } catch(e) {
                console.error("ERROR " + e);
            }
            return text;
        };
    });

