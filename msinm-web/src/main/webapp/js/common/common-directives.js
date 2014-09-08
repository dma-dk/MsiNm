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
            template: "<img height='16'/>",
            replace: true,
            scope: {
                lang: "=",
                style: "@"
            },
            link: function(scope, element, attrs) {
                scope.$watch(function() {
                        return scope.lang;
                    },
                    function(newValue) {
                        if (newValue) {
                            element.attr('src', '/img/flags/' + newValue + '.png');
                        }
                    }, true);

                if (scope.style) {
                    element.attr('style', scope.style);
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


    .directive('msiAttachment', [function () {
        return {
            restrict: 'E',
            templateUrl: '/partials/common/attachment.html',
            replace: true,
            scope: {
                file: "=",
                size: "@",
                clickable: "@"
            },
            link: function(scope, element, attrs) {
                scope.thumbnailUrl = "/rest/repo/thumb/" + scope.file.path + "?size=" + scope.size;
                scope.fileUrl = "/rest/repo/file/" + scope.file.path;
                scope.imageClass = "attachment-image size-" + scope.size;
            }
        };
    }])


    /**
     * Replaces the content of the element with the area description of the message
     */
    .directive('msiMessageArea', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiMessageArea: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch(
                    function() { return scope.msiMessageArea; },
                    function(newValue) {
                        var desc = LangService.desc(scope.msiMessageArea);
                        var areas = (desc && desc.vicinity) ? desc.vicinity : '';
                        for (var area = scope.msiMessageArea.area; area; area = area.parent) {
                            desc = LangService.desc(area);
                            var areaName = (desc) ? desc.name : '';
                            areas = areaName + ((areas.length > 0 && areaName.length > 0) ? ' - ' : '') + areas;
                        }
                        element.html(areas);
                }, true);
            }
        };
    }])


    /**
     * Replaces the content of the element with the area description
     */
    .directive('msiArea', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiArea: "=",
                areaDivider: "@"
            },
            link: function(scope, element, attrs) {
                var divider = (attrs.areaDivider) ? attrs.areaDivider : " - "
                scope.$watch(
                    function() { return scope.msiArea; },
                    function (newValue) {
                        var areas = '';
                        for (var area = scope.msiArea; area; area = area.parent) {
                            desc = LangService.desc(area);
                            var areaName = (desc) ? desc.name : '';
                            areas = areaName + ((areas.length > 0 && areaName.length > 0) ? divider : '') + areas;
                        }
                        element.html(areas);
                    });
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
                scope.$watch(
                    function () {
                        return scope.msiMessageId;
                    },
                    function (newValue) {
                        var msg = scope.msiMessageId;
                        var id = msg.seriesIdentifier.fullId;
                        if (msg.type == 'TEMPORARY_NOTICE') {
                            id += '(T)';
                        } else if (msg.type == 'PRELIMINARY_NOTICE') {
                            id += '(P)';
                        }
                        element.html(id);
                    });
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
     * Angular implementation of the bootstrap ScrollSpy component.
     * From: http://stackoverflow.com/questions/17470370/how-to-implement-a-scrollspy-in-angular-js-the-right-way
     */
    .directive('scrollSpy', function ($window) {
        return {
            restrict: 'A',
            controller: function ($scope) {
                $scope.spies = [];
                this.addSpy = function (spyObj) {
                    $scope.spies.push(spyObj);
                };
            },
            link: function (scope, elem, attrs) {
                var spyElems;
                spyElems = [];

                scope.$watch('spies', function (spies) {
                    var spy, _i, _len, _results;
                    _results = [];

                    for (_i = 0, _len = spies.length; _i < _len; _i++) {
                        spy = spies[_i];

                        if (spyElems[spy.id] == null) {
                            _results.push(spyElems[spy.id] = elem.find('#' + spy.id));
                        }
                    }
                    return _results;
                });

                $($window).scroll(function () {
                    var highlightSpy, pos, spy, _i, _len, _ref;
                    highlightSpy = null;
                    _ref = scope.spies;

                    // cycle through `spy` elements to find which to highlight
                    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                        spy = _ref[_i];
                        spy.out();

                        // catch case where a `spy` does not have an associated `id` anchor
                        if (spyElems[spy.id].offset() === undefined) {
                            continue;
                        }

                        if ((pos = spyElems[spy.id].offset().top) - $window.scrollY <= 0) {
                            // the window has been scrolled past the top of a spy element
                            spy.pos = pos;

                            if (highlightSpy == null) {
                                highlightSpy = spy;
                            }
                            if (highlightSpy.pos < spy.pos) {
                                highlightSpy = spy;
                            }
                        }
                    }

                    // select the last `spy` if the scrollbar is at the bottom of the page
                    if ($(window).scrollTop() + $(window).height() >= $(document).height()) {
                        spy.pos = pos;
                        highlightSpy = spy;
                    }

                    return highlightSpy != null ? highlightSpy["in"]() : void 0;
                });
            }
        };
    })

    .directive('spy', function ($location, $anchorScroll) {
        return {
            restrict: "A",
            require: "^scrollSpy",
            link: function(scope, elem, attrs, affix) {
                elem.click(function () {
                    $location.hash(attrs.spy);
                    $anchorScroll();
                });

                affix.addSpy({
                    id: attrs.spy,
                    in: function() {
                        elem.addClass('active');
                    },
                    out: function() {
                        elem.removeClass('active');
                    }
                });
            }
        };
    })

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
    .directive('msiFileUpload', ['FileUploader', 'Auth', function (FileUploader, Auth) {
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
                return {
                    pre: function (scope, element, attrs) {
                        // create a uploader with options
                        scope.uploader = new FileUploader({
                            scope: scope,
                            url: scope.repoFolder,
                            data: { uploadData: scope.data },
                            filters: []
                        });
                    },

                    post: function (scope, element, attrs) {

                        if (scope.data) {
                            scope.uploader.onBeforeUploadItem = function (item) {
                                item.formData.push({ data: JSON.stringify(scope.data) });
                            };
                        }

                        // Check if file-types are defined
                        if (scope.fileTypes) {
                            scope.uploader.filters.push({
                                name: 'filterName',
                                fn: function (item, options) {
                                    var ext = item.name.extension();
                                    return ext && $.inArray(ext.toLowerCase(), scope.fileTypes.toLowerCase().split(",")) > -1;
                                }});
                        }

                        // Auto-upload
                        if (scope.autoUpload) {
                            scope.uploader.autoUpload = scope.autoUpload;
                        }

                        // Remove after upload
                        if (scope.removeAfterUpload) {
                            scope.uploader.removeAfterUpload = scope.removeAfterUpload;
                        }

                        // Handle authenticaiton
                        if (Auth.isLoggedIn()) {
                            scope.uploader.headers.Authorization = Auth.authorizationHeader();
                        }

                        scope.cancelOrRemove = function (item) {
                            if (item.isUploading) {
                                item.cancel();
                            } else {
                                item.remove();
                            }
                        };

                        scope.$watch(function () {
                            return scope.repoFolder;
                        }, function (value) {
                            scope.uploader.url = value;
                        }, true);

                        // Success call-back
                        if (scope.success) {
                            scope.uploader.onSuccessItem = function (item, response, status, headers) {
                                scope.success({ result: response});
                            };
                        }

                        // Error call-back
                        if (scope.error) {
                            scope.uploader.onErrorItem = function (item, response, status, headers) {
                                scope.error({ status: status, statusText: response.statusText });
                            };
                        }
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
                sort : '@',
                entitySelected : '&',
                entityMoved : '&'
            },

            link: function (scope, element, attrs, ngModel) {

                scope.sort = (attrs.sort !== undefined) ? attrs.sort : false;

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
                    if (scope.sort) {
                        tree.rootNode.sortChildren(null, true);
                    }
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
    }])

    /**
     * Wraps the bootstrap-datepicker plugin
     */
    .directive('msiDatePicker', ['$parse', function ($parse) {
        'use strict';

        return {
            restrict: 'A',
            scope: {
                msiDatePicker: '=',
                format: '@'
            },
            link: function (scope, elm, attrs, ctrl) {

                var format = "dd-mm-yyyy";
                if (attrs.format) {
                    format = attrs.format;
                }

                // Init the picker and detect picker selection
                var picker = elm.datepicker({ format: format })
                    .on('changeDate', function(ev) {
                        picker.hide();
                        scope.$apply(function (scope) {
                            scope.msiDatePicker = ev.date.getTime();
                        });
                    })
                    .data('datepicker');

                // Detect changes in the input field
                elm.bind('change', function(event) {
                    if (!elm.val()) {
                        scope.$apply(function (scope) {
                            scope.msiDatePicker = undefined;
                        });
                    } else {
                        scope.$apply(function (scope) {
                            elm.datepicker('setValue', elm.val()).datepicker('update');
                            scope.msiDatePicker = picker.date.getTime();
                        });
                    }
                });

                // Watch changes in the model value
                scope.$watch(function () {
                    return scope.msiDatePicker
                }, function (val) {
                    if (val) {
                        try {
                            var date = new Date(val);
                            /*
                            // Calling format() will throw an exception if it cannot be formatted
                            date.formatDate(format);

                            // Check that the year range is sensible
                            if (date.getFullYear() < 1900 || date.getFullYear() > 2100) {
                                throw new Error("Invalid date format.");
                            }
                            */
                            elm.datepicker('setValue', date).datepicker('update');
                        } catch (e) {
                            elm.datepicker({date: null}).datepicker('update');
                        }
                    } else {
                        elm.datepicker({date: null}).datepicker('update').val('');
                    }
                });
            }
        }
    }]);


