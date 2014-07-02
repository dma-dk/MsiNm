/**
 * Common directives.
 */
angular.module('msinm.common')

    .directive('focus', ['$timeout', function ($timeout) {
        'use strict';

        return function (scope, element, attrs) {
            scope.$watch(attrs.focus, function (newValue) {
                $timeout(function () {
                    newValue && element.focus();
                }, 100);
            }, true);
        };
    }])

    .directive('dynamic', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            replace: true,
            link: function (scope, ele, attrs) {
                scope.$watch(attrs.dynamic, function (html) {
                    ele.html(html);
                    $compile(ele.contents())(scope);
                });
            }
        };
    }])

    .directive('flag', [function () {
        return {
            restrict: 'E',
            template: "<img height='16' ng-click='copyText()'/>",
            replace: true,
            scope: {
                country: "=",
                copyFrom: "@",
                copyTo: "@",
                style: "@"
            },
            link: function(scope, element, attrs) {
                scope.$watch(function() {
                        return scope.country;
                    },
                    function(newValue) {
                        if (newValue) {
                            element.attr('src', '/img/flags/' + newValue + '.png');
                        }
                    }, true);

                if (scope.style) {
                    element.attr('style', scope.style);
                }

                scope.copyText = function() {
                    if (scope.copyFrom && scope.copyTo) {
                        $('#' + scope.copyTo).val($('#' + scope.copyFrom).val());
                    }
                }
            }
        };
    }])

    .directive('langFlag', ['$rootScope', function ($rootScope) {
        return {
            restrict: 'A',
            scope: {
                langFlag: "=",
                flagVisible: "="
            },
            link: function(scope, element, attrs) {
                element.addClass("localized");
                element.css({
                    background: "white url('/img/flags/" + scope.langFlag + ".png') no-repeat 99% 0%",
                    backgroundSize: "auto 14px"
                });
            }
        };
    }])

    /**
     * Replaces the content of the element with the area description of the message
     */
    .directive('msiArea', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiArea: "="
            },
            link: function(scope, element, attrs) {
                if (attrs.msiArea) {
                    var desc = LangService.desc(scope.msiArea);
                    var areas = (desc && desc.vicinity) ? desc.vicinity : '';
                    for (var area = scope.msiArea.area; area; area = area.parent) {
                        desc = LangService.desc(area);
                        var areaName = (desc) ? desc.name : '';
                        areas = areaName + ((areas.length > 0 && areaName.length > 0) ? ' - ' : '') + areas;
                    }
                    element.html(areas);
                }
            }
        };
    }])

    /**
     * Displays the composite identifier of the message
     */
    .directive('msiMessageId', [function () {
        return {
            restrict: 'A',
            scope: {
                msiMessageId: "="
            },
            link: function(scope, element, attrs) {
                if (attrs.msiMessageId) {
                    var id = '';
                    var msg = scope.msiMessageId;
                    var type = msg.type;
                    if (type == 'COSTAL_WARNING' || type == 'SUBAREA_WARNING' || type == 'NAVAREA_WARNING') {
                        // MSI
                        id = 'MSI ' + msg.seriesIdentifier.number + '-' + msg.seriesIdentifier.year;
                    } else {
                        // NTM
                        id = 'NM ' + msg.seriesIdentifier.number + '-' + msg.seriesIdentifier.year;
                        if (type == 'TEMPORARY_NOTICE') {
                            id += '(T)';
                        } else if (type == 'PRELIMINARY_NOTICE') {
                            id += '(P)';
                        }
                    }
                    element.html(id);
                }
            }
        };
    }])

/**
     * Show element active/inactive depending on the current location.
     * Usage:
     * <pre>
     *     <li check-active="/search/*"><a href="search.html">Search</a></li>
     * </pre>
     * <p>
     * Inspired by:
     *   http://stackoverflow.com/questions/16199418/how-do-i-implement-the-bootstrap-navbar-active-class-with-angular-js
     * - but changed quite a bit.
     */
    .directive('checkActive', [ '$location', function ($location) {
        'use strict';

        return {
            restrict: 'A',
            scope: {
                checkActive: "@"
            },
            link: function (scope, element, attrs) {

                // Watch for the $location
                scope.$watch(function () {
                    return $location.path();
                }, function (newValue, oldValue) {

                    var locMask = scope.checkActive.split("*").join(".*");
                    var regexp = new RegExp('^' + locMask + '$', ['i']);

                    if (regexp.test(newValue)) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                });
            }
        };
    }])

    /**
     * Checks that two password fields match up and that the password is strong
     * Based on http://jsfiddle.net/EHJq8/
     */
    .directive('pwCheck', ['$parse', function ($parse) {
        'use strict';

        return {
            require: 'ngModel',
            restrict: 'A',
            link: function (scope, elem, attrs, ctrl) {

                //This part does the matching
                scope.$watch(function() {
                    return (ctrl.$pristine && angular.isUndefined(ctrl.$modelValue)) || $parse(attrs.pwCheck)(scope) === ctrl.$modelValue;
                }, function(currentValue) {
                    ctrl.$setValidity('pwmatch', currentValue);
                });

                //This part is supposed to check the strength
                ctrl.$parsers.unshift(function(viewValue) {
                    var pwdValidLength, pwdHasLetter, pwdHasNumber;

                    pwdValidLength = (viewValue && viewValue.length >= 6 ? true : false);
                    pwdHasLetter = (viewValue && /[A-z]/.test(viewValue)) ? true : false;
                    pwdHasNumber = (viewValue && /\d/.test(viewValue)) ? true : false;

                    ctrl.$setValidity('pwvalid', pwdValidLength && pwdHasLetter && pwdHasNumber);

                    return viewValue;
                });
            }
        }
    }])


    /**
     * File upload, based on:
     * https://github.com/nervgh/angular-file-upload
     * <p>
     * The directive takes the following attributes:
     * <ul>
     *   <li>repo-folder: The folder wihtin the repo. Mandatory.</li>
     *   <li>multiple: Support single or multiple file upload. Defaults to false.</li>
     *   <li>auto-upload: Automatically start upload. Defaults to false.</li>
     *   <li>remove-after-upload: Remove file from queue once uploaded. Defaults to false.</li>
     *   <li>success(result): Success callback function. Optional.</li>
     *   <li>error(status, statusText): Error callback function. Optional.</li>
     * </ul>
     */
    .directive('msiFileUpload', ['$fileUploader', 'Auth', function ($fileUploader, Auth) {
        'use strict';

        return {
            restrict: 'AE',

            transclude: true,

            templateUrl: '/partials/common/file-upload.html',

            scope: {
                repoFolder:         '=repoFolder',
                multiple:           '=multiple',
                dropText:           '@dropText',
                fileTypes:          '=fileTypes',
                autoUpload:         '=autoUpload',
                removeAfterUpload:  '=removeAfterUpload',
                data:               '=data',
                success:            '&success',
                error:              '&error'
            },

            compile: function(element, attrs) {
                if (attrs.dropText == undefined) {
                    attrs.$set("dropText", (attrs.multiple) ? 'or drop files here' : 'or drop file here');
                }

                // Return link function
                return function (scope, element, attrs) {
                    // create a uploader with options
                    var uploader = scope.uploader = $fileUploader.create({
                        scope: scope,
                        url: scope.repoFolder,
                        data: { uploadData: scope.data },
                        filters: []
                    });

                    if (scope.data) {
                        uploader.bind('beforeupload', function (event, item) {
                            item.formData.push({ data: JSON.stringify(scope.data) });
                        });
                    }

                    // Check if file-types are defined
                    if (scope.fileTypes) {
                        uploader.filters.push(function(item) {
                            return $.inArray(item.name.extension(), scope.fileTypes.split(",")) > -1;
                        });
                    }

                    // Auto-upload
                    if (scope.autoUpload) {
                        uploader.autoUpload = scope.autoUpload;
                    }

                    // Remove after upload
                    if (scope.removeAfterUpload) {
                        uploader.removeAfterUpload = scope.removeAfterUpload;
                    }

                    // Handle authenticaiton
                    if (Auth.isLoggedIn()) {
                        uploader.headers.Authorization = Auth.authorizationHeader();
                    }

                    scope.cancelOrRemove = function(item) {
                        if (item.isUploading) {
                            item.cancel();
                        } else {
                            item.remove();
                        }
                    };

                    scope.$watch(attrs.repoFolder, function (value) {
                        uploader.url = value;
                    }, true);

                    // Success call-back
                    if (scope.success) {
                        uploader.bind('success', function (event, xhr, item, response) {
                            scope.success({ result: response});
                        });
                    }

                    // Error call-back
                    if (scope.error) {
                        uploader.bind('error', function (event, xhr, item, response) {
                            scope.error({ status: xhr.status, statusText: xhr.statusText });
                        });
                    }
                }

            }

        }
    }])

    /**
     * Directive that wraps the fancytree jQuery plugin
     */
    .directive('msiEntityTree', [ function () {
        'use strict';

        return {
            restrict: 'AE',
            scope: {
                entities: '=',
                filter: '=',
                entitySelected : '&',
                entityMoved : '&'
            },

            link: function (scope, element, attrs, ngModel) {

                // Initialize the tree
                element.fancytree({
                    source: [],
                    keyboard: true,
                    extensions: ["filter", "dnd"],
                    filter: {
                        mode: "hide"
                    },
                    dnd: {
                        autoExpandMS: 400,
                        draggable: {
                            zIndex: 1000,
                            scroll: false
                        },
                        preventVoidMoves: true,
                        preventRecursiveMoves: true,
                        dragStart: function(node, data) { return true; },
                        dragEnter: function(node, data) {
                            if (node.parent === data.otherNode.parent) {
                                return ['over'];
                            }
                            return true;
                        },
                        dragOver: function(node, data) {},
                        dragLeave: function(node, data) {},
                        dragStop: function(node, data) {},
                        dragDrop: function(node, data) {
                            handleDragDrop(node, data);
                        }
                    },
                    activate: function(event, data){
                        var node = data.node;
                        if (scope.entitySelected) {
                            scope.entitySelected({ entity: node.data.entity });
                        }
                    }
                });

                var tree = element.fancytree("getTree");

                /**
                 * Convert the list of entities into the tree structure used by
                 * https://github.com/mar10/fancytree/
                 */
                function toTreeData(entities, treeData, level) {
                    for (var i in entities) {
                        var entity = entities[i];
                        var title = (entity.descs && entity.descs.length > 0) ? entity.descs[0].name : 'N/A';
                        var node = { key: entity.id, title: title, folder: true, children: [], level: level, entity: entity };
                        treeData.push(node);
                        if (entity.children && entity.children.length > 0) {
                            toTreeData(entity.children, node.children, level + 1);
                        }
                    }
                }

                function handleDragDrop(node, data) {
                    if (scope.entityMoved) {
                        var entity = data.otherNode.data.entity;
                        var parent = undefined;
                        if (data.hitMode == 'before' || data.hitMode == 'after') {
                            parent = (node.parent.data.entity) ? node.parent.data.entity : undefined;
                        } else if (data.hitMode == 'over') {
                            parent = node.data.entity;
                        }
                        scope.entityMoved({ entity: entity, parent: parent });

                    } else {
                        data.otherNode.moveTo(node, data.hitMode);
                    }
                }

                // Watch entities
                scope.$watchCollection(function () {
                    return scope.entities;
                }, function (newValue) {
                    if (tree.options.source && tree.options.source.length > 0) {
                        scope.storeState();
                    }
                    var treeData = [];
                    if (newValue) {
                        toTreeData(newValue, treeData, 0);
                    }
                    tree.options.source = treeData;
                    tree.reload();
                    tree.rootNode.sortChildren(null, true);
                    tree.clearFilter();
                    scope.collapseAll();
                    scope.restoreState();
                    if (scope.filter) {
                        tree.filterNodes(scope.filter);
                    }
                });

                // Watch the filter
                if (attrs.filter) {
                    scope.$watch(function () {
                        return scope.filter
                    }, function (newValue) {
                        var val = newValue || '';
                        tree.filterNodes(val);
                        if (val != '') {
                            scope.expandAll();
                        } else {
                            scope.collapseAll();
                        }
                    }, true);
                }

                scope.storeState = function() {
                    scope.expandedIds = [];
                    scope.activeKey = tree.getActiveNode() ? tree.getActiveNode().key : undefined;
                    tree.visit(function(node){
                        if (node.expanded) {
                            scope.expandedIds.push(node.key);
                        }
                    });
                };

                scope.restoreState = function() {
                    if (scope.expandedIds) {
                        tree.visit(function(node){
                            node.setExpanded($.inArray(node.key, scope.expandedIds) > -1);
                        });
                    }
                    if (scope.activeKey) {
                        tree.activateKey(scope.activeKey);
                    }
                };

                scope.collapseAll = function() {
                    // Collapse all nodes except the root node
                    tree.visit(function(node){
                        node.setExpanded(node.data.level == 0);
                    });

                };

                scope.expandAll = function() {
                    tree.visit(function(node){
                        node.setExpanded(true);
                    });
                };

            }

        };
    }]);



















