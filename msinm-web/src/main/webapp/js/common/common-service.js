/**
 * Common services.
 */
angular.module('msinm.common')

/**
 * The modalService is very much inspired by (even copied from):
 * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
 */
.service('DialogService', ['$modal',
        function ($modal) {
        'use strict';

        var modalDefaults = {
            backdrop: true,
            keyboard: true,
            modalFade: true,
            templateUrl: '/partials/common/dialog.html'
        };

        var modalOptions = {
            closeButtonText: 'Cancel',
            actionButtonText: 'OK',
            headerText: '',
            bodyText: undefined
        };

        this.showDialog = function (customModalDefaults, customModalOptions) {
            if (!customModalDefaults) {
                customModalDefaults = {};
            }
            customModalDefaults.backdrop = 'static';
            return this.show(customModalDefaults, customModalOptions);
        };

        this.showConfirmDialog = function (headerText, bodyText) {
            return this.showDialog(undefined, { headerText: headerText, bodyText: bodyText});
        };

        this.show = function (customModalDefaults, customModalOptions) {
            //Create temp objects to work with since we're in a singleton service
            var tempModalDefaults = {};
            var tempModalOptions = {};

            //Map angular-ui modal custom defaults to modal defaults defined in service
            angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

            //Map modal.html $scope custom properties to defaults defined in service
            angular.extend(tempModalOptions, modalOptions, customModalOptions);

            if (!tempModalDefaults.controller) {
                tempModalDefaults.controller = function ($scope, $modalInstance) {
                    $scope.modalOptions = tempModalOptions;
                    $scope.modalOptions.ok = function (result) {
                        $modalInstance.close(result);
                    };
                    $scope.modalOptions.close = function (result) {
                        $modalInstance.dismiss('cancel');
                    };
                }
            }

            return $modal.open(tempModalDefaults).result;
        };

    }])

/**
 * The language service is used for changing language, etc.
 */
.service('LangService', ['$rootScope', '$window', '$translate',
        function ($rootScope, $window, $translate) {
        'use strict';

        this.changeLanguage = function(lang) {
            $translate.use(lang);
            $rootScope.language = lang;
            $window.localStorage.lang = lang;
        };

        // look for a description entity with the given language
        this.descForLanguage = function(elm, lang) {
            if (elm && elm.descs) {
                for (var d in elm.descs) {
                    if (elm.descs[d].lang == lang) {
                        return elm.descs[d];
                    }
                }
            }
            return undefined;
        };

        // look for a description entity with the given language - falls back to using the first description
        this.descForLangOrDefault = function(elm, lang) {
            var desc = this.descForLanguage(elm, (lang) ? lang : $rootScope.language);
            if (!desc && elm && elm.descs && elm.descs.length > 0) {
                desc = elm.descs[0];
            }
            return desc;
        };

        // look for a description entity with the current language
        this.desc = function(elm) {
            return this.descForLanguage(elm, $rootScope.language);
        };


        // Ensures that elm.descs contain a description entity for each supported language
        // The initFunc will be called for newly added description entities and should be used
        // to initialize the fields to include, e.g. "description" or "name",...
        // Optionally, an oldElm can be specified, from which the description entity will be picked
        // if present
        this.checkDescs = function (elm, initFunc, oldElm, languages) {
            if (!elm.descs) {
                elm.descs = [];
            }
            if (!languages || languages.length == 0) {
                languages = $rootScope.modelLanguages;
            }
            for (var l in languages) {
                var lang = languages[l];
                var desc = this.descForLanguage(elm, lang);
                if (!desc && oldElm) {
                    desc = this.descForLanguage(oldElm, lang);
                    if (desc) {
                        elm.descs.push(desc);
                    }
                }
                if (!desc) {
                    desc = { 'lang': lang };
                    initFunc(desc);
                    elm.descs.push(desc);
                }
            }
            // Lastly, sort by language
            this.sortDescs(elm);
            return elm;
        };

        // Computes a sort value by comparing the desc language to the
        // current language or else the index in the list of available languages.
        function sortValue(desc) {
            if (!desc.lang){
                return 1000;
            } else if (desc.lang == $rootScope.language) {
                return -1;
            }
            var index = $.inArray(desc, $rootScope.languages);
            return (index == -1) ? 999 : index;
        }

        // Sort the localized description entities by language
        this.sortDescs = function (elm) {
            if (elm && elm.descs) {
                elm.descs.sort(function(d1, d2){
                    return sortValue(d1) - sortValue(d2);
                });
            }
        }
    }]);
