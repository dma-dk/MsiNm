/**
 * The message search controller for the app.
 */
angular.module('msinm.search')

    /**
     * Controller that handles editing messages
     */
    .controller('MessageEditorCtrl', ['$scope', '$rootScope', '$routeParams', '$modal', '$timeout', '$window', 'growlNotifications', 'MessageService', 'LangService',
        function ($scope, $rootScope, $routeParams, $modal, $timeout, $window, growlNotifications, MessageService, LangService) {
            'use strict';

            $scope.dateFormat = "dd-mm-yyyy";
            $scope.today = new Date().formatDate($scope.dateFormat);

            $scope.attachments = [];
            $scope.uploadUri = '/rest/repo/upload-temp/';
            $scope.attachments = [];

            $scope.messageId = ($routeParams.messageId && $routeParams.messageId != 'new') ? $routeParams.messageId : undefined;

            $scope.msg = { seriesIdentifier: { mainType: 'MSI' }, descs: [], locations: [], areadId: undefined };

            // The locationsLoaded is used to trigger the location editor
            // and get it to initialize the locaiton list
            $scope.locationsLoaded = false;

            // This will be set when the "Save message" button is clicked and will
            // disable the button, to avoid double-clicks
            $scope.messageSaved = false;


            // Check for changes in the locations
            $scope.$watch(
                function() { return $scope.msg; },
                function() { $scope.editForm.$setDirty(); },
                true);


            // Check for changes in the main type
            $scope.$watch(
                function() { return $scope.msg.seriesIdentifier.mainType; },
                function() {
                    if ($scope.msg.seriesIdentifier.mainType == 'NM') {
                        if ($.inArray($scope.msg.type, ['PERMANENT_NOTICE', 'TEMPORARY_NOTICE', 'PRELIMINARY_NOTICE', 'MISCELLANEOUS_NOTICE']) == -1) {
                            $scope.msg.type = 'TEMPORARY_NOTICE';
                        }
                    } else if ($scope.msg.seriesIdentifier.mainType == 'MSI') {
                        if ($.inArray($scope.msg.type, ['COSTAL_WARNING', 'SUBAREA_WARNING', 'NAVAREA_WARNING']) == -1) {
                            $scope.msg.type = 'SUBAREA_WARNING';
                        }
                    }
                },
                true);


            // When the location editor detects a new locations list, it will
            // add blank desc records, and thus, yield to form dirty.
            // So, after loading a message, call this method that waits 100ms before
            // setting the form pristine
            $scope.setPristine = function () {
                $timeout(function () {
                    $scope.editForm.$setPristine();
                }, 500);
            };


            // Copies the locations from the selected area to the message
            $scope.copyAreaLocations = function() {
                if ($scope.msg.areaId) {
                    $scope.locationsLoaded = false;
                    MessageService.getArea(
                        $scope.msg.areaId,
                        function (data) {
                            $scope.msg.locations = data.locations ? data.locations : [];
                            $scope.locationsLoaded = true; // Trigger the location editor
                        },
                        function (data) {
                            growlNotifications.add('<h4>Area Lookup Failed</h4>', 'danger', 3000);
                        }
                    )
                }
            };


            // Translate the time of the first descriptor to the other descriptors and
            // computes the validFrom and validTo dates from the field.
            $scope.translateTime = function () {
                var time = {
                    validFrom: $scope.msg.validFrom,
                    validTo: $scope.msg.validTo,
                    times: []
                };
                for (var i in $scope.msg.descs) {
                    var desc = $scope.msg.descs[i];
                    time.times.push({ lang: desc.lang, time: desc.time });
                }
                MessageService.translateTime(
                    time,
                    function (data) {
                        var dirty = false;
                        if ($scope.validFrom != data.validFrom) {
                            $scope.validFrom = data.validFrom;
                            dirty = true;
                        }
                        if ($scope.validTo != data.validTo) {
                            $scope.validTo = data.validTo;
                            dirty = true;
                        }
                        for (var i in data.times) {
                            if ($scope.msg.descs[i].time != data.times[i].time) {
                                $scope.msg.descs[i].time = data.times[i].time;
                                dirty = true;
                            }
                        }
                        if (dirty) {
                            $scope.editForm.$setDirty();
                        }
                    },
                    function (data) {
                        growlNotifications.add('<h4>Time Translation Failed</h4>', 'danger', 3000);
                    }
                );
            };


            // Deletes the given reference from the list of message references
            $scope.deleteReference = function (ref) {
                if ($.inArray(ref, $scope.msg.references) > -1) {
                    $scope.msg.references.splice( $.inArray(ref, $scope.msg.references), 1 );
                    $scope.editForm.$setDirty();
                }
            };


            // Adds the new reference to the list of message references
            $scope.addReference = function () {
                var id = $scope.parseNewRef();
                if (id) {
                    if (!$scope.msg.references) {
                        $scope.msg.references = [];
                    }
                    var ref = { seriesIdentifier: id, type: $scope.newRef.type };
                    $scope.msg.references.push(ref);
                    $scope.newRef = { id: '', type: 'REFERENCE' };
                }
            };


            // Parses the new reference as a series identifier
            $scope.parseNewRef = function() {
                var parts = ($scope.newRef) ? $scope.newRef.id.toUpperCase().split('-') : [];
                if (parts.length == 4 && /^(MSI)|(NM)$/.test(parts[0]) && !isNaN(parts[2]) && !isNaN(parts[3]) && parts[3].length == 2) {
                    var id = {
                        mainType: parts[0],
                        authority: parts[1],
                        number: parseInt(parts[2]),
                        year: 2000 + parseInt(parts[3]),
                        fullId: $scope.newRef.id.toUpperCase()
                    };
                    return id;
                }
                return undefined;
            }


            // Ensure the message structure is valid and initialized
            $scope.initMessage = function () {
                $window.scrollTo(0,0);

                var msg = $scope.msg;

                if (!msg.seriesIdentifier) {
                    msg.seriesIdentifier = { mainType: 'MSI' };
                    msg.type = 'SUBAREA_WARNING';
                }

                LangService.checkDescs(
                    msg,
                    function(desc) {
                        desc.title = '';
                        desc.description = '';
                        // TODO...
                    },
                    undefined,
                    $scope.languages);

                if (msg.area) {
                    msg.areaId = msg.area.id;
                    $("#editorArea").select2("data", { id: msg.area.id, text: msg.area.descs[0].name, area: msg.area });
                } else {
                    $("#editorArea").select2("data", null);
                }

                if (msg.categories && msg.categories.length > 0) {
                    var data = [];
                    msg.categoryIds = '';
                    for (var i in msg.categories) {
                        var cat = msg.categories[i];
                        if (msg.categoryIds == '') {
                            msg.categoryIds += ',';
                        }
                        msg.categoryIds += cat.id;
                        data.push({id: cat.id, text: cat.descs[0].name, category: cat });
                    }
                    $("#editorCategories").select2("data", data);
                } else {
                    $("#editorCategories").select2("data", null);
                }

                if (msg.charts && msg.charts.length > 0) {
                    var data = [];
                    msg.chartIds = '';
                    for (var i in msg.charts) {
                        var chart = msg.charts[i];
                        if (msg.chartIds == '') {
                            msg.chartIds += ',';
                        }
                        msg.chartIds += chart.id;
                        data.push({id: chart.id, text: chart.fullChartNumber, chart: chart });
                    }
                    $("#editorCharts").select2("data", data);
                } else {
                    $("#editorCharts").select2("data", null);
                }

                // Load attachments
                $scope.listFiles();

                $scope.newRef = { id: '', type: 'REFERENCE' };

                $scope.locationsLoaded = true;  // Trigger the location editor
                $scope.messageSaved = false; // Remove lock on save button
                $scope.setPristine();
            };


            // Load the message details for the given message id
            $scope.loadMessageDetails = function() {
                if ($scope.messageId) {
                    MessageService.allDetails(
                        $scope.messageId,
                        function (data) {
                            $scope.msg = data;
                            $scope.uploadUri = '/rest/repo/upload/' + $scope.msg.repoPath;
                            $scope.initMessage();
                        },
                        function (data) {
                            growlNotifications.add('<h4>Message Lookup Failed</h4>', 'danger', 3000);
                        });
                } else {
                    MessageService.newMessageTemplate(
                        function (data) {
                            $scope.msg = data;
                            $scope.uploadUri = '/rest/repo/upload-temp/' + $scope.msg.repoPath;
                            $scope.initMessage();
                        },
                        function (data) {
                            console.error("Error getting new temp dir" + data);
                        });
                }
            };


            // Load the message details
            $scope.loadMessageDetails();


            // Save the current message
            $scope.saveMessage = function () {

                // Prevent double-submissions
                $scope.messageSaved = true;

                // Update area
                var area = $("#editorArea").select2("data");
                $scope.msg.area = area;
                if (area) {
                    area.parent = null; // Trim json
                }

                // Update Categories
                var categories = $("#editorCategories").select2("data");
                $scope.msg.categories = [];
                for (var i in categories) {
                    $scope.msg.categories.push(categories[i]);
                    categories[i].parent = null; // Trim json
                }

                // Update charts
                var charts = $("#editorCharts").select2("data");
                $scope.msg.charts = [];
                for (var j in charts) {
                    $scope.msg.charts.push(charts[j]);
                    charts[i].parent = null; // Trim json
                }

            };


            // Reload the message details
            $scope.reloadMessage = function () {
                $scope.loadMessageDetails();
            }


            // Fetches the attachments belonging the the current message
            $scope.listFiles = function() {
                MessageService.listFiles(
                    $scope.msg.repoPath,
                    function (data) {
                        $scope.attachments = data;
                        if(!$scope.$$phase) {
                            $scope.$apply();
                        }
                    },
                    function (data) {
                        console.error("Error listing files in " + $scope.msg.repoPath);
                    });
            };


            // Callback, called when an attachment has been uploaded
            $scope.attachmentUploaded = function(result) {
                $scope.listFiles();
                if(!$scope.$$phase) {
                    $scope.$apply();
                }
            };


            // Configuation of the TinyMCE editors
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
                    var scope = angular.element($("#message-editor")).scope();
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

        }])


    /**
     * Controller that handles displaying message details
     */
    .controller('MessageDetailsCtrl', ['$scope', '$modal',
        function ($scope, $modal) {
            'use strict';

            function extractMessageIds(messages) {
                var ids = [];
                if (messages) {
                    for (var i in messages) {
                        ids.push(messages[i].id);
                    }
                }
                return ids;
            }

            $scope.$on('messageDetails', function (event, data) {
                $modal.open({
                    controller: "MessageDialogCtrl",
                    templateUrl: "/partials/search/message-details.html",
                    size: 'lg',
                    resolve: {
                        messageId: function () {
                            return data.messageId;
                        },
                        messages: function () {
                            return extractMessageIds(data.messages);
                        }
                    }
                });
            });

        }])


    /**
     * Controller that handles displaying message details in a dialog
     */
    .controller('MessageDialogCtrl', ['$scope', '$window', 'growlNotifications', 'MessageService', 'messageId', 'messages',
        function ($scope, $window, growlNotifications, MessageService, messageId, messages) {
            'use strict';

            $scope.warning = undefined;
            $scope.messages = messages;
            $scope.pushedMessageIds = [];
            $scope.pushedMessageIds[0] = messageId;

            $scope.msg = undefined;
            $scope.index = $.inArray(messageId, messages);
            $scope.showNavigation = $scope.index >= 0;

            // Attempt to improve printing
            $("body").addClass("no-print");
            $scope.$on("$destroy", function() {
                $("body").removeClass("no-print");
            });

            $scope.selectPrev = function() {
                if ($scope.pushedMessageIds.length == 1 && $scope.index > 0) {
                    $scope.index--;
                    $scope.pushedMessageIds[0] = $scope.messages[$scope.index];
                    $scope.loadMessageDetails();
                }
            };

            $scope.selectNext = function() {
                if ($scope.pushedMessageIds.length == 1 && $scope.index >= 0 && $scope.index < $scope.messages.length - 1) {
                    $scope.index++;
                    $scope.pushedMessageIds[0] = $scope.messages[$scope.index];
                    $scope.loadMessageDetails();
                }
            };

            $scope.selectMessage = function (messageId) {
                $scope.pushedMessageIds.push(messageId);
                $scope.loadMessageDetails();
            };

            $scope.back = function () {
                if ($scope.pushedMessageIds.length > 1) {
                    $scope.pushedMessageIds.pop();
                    $scope.loadMessageDetails();
                }
            };

            $scope.pdf = function () {
                var messageId = $scope.pushedMessageIds[$scope.pushedMessageIds.length - 1];
                $window.location = '/rest/messages/message-pdf/' + messageId + '.pdf?lang=' + $scope.language;
            };

            $scope.calendar = function () {
                var messageId = $scope.pushedMessageIds[$scope.pushedMessageIds.length - 1];
                $window.location = '/rest/messages/message-cal/' + messageId + '.ics?lang=' + $scope.language;
            };

            $scope.edit = function() {
                $scope.$dismiss('edit');
                //$scope.go('/search/edit/' + $scope.msg.id);
                $window.location = '/search.html#/search/edit/' + $scope.msg.id;
            };

            // Load the message details for the given message id
            $scope.loadMessageDetails = function() {

                MessageService.details(
                    $scope.pushedMessageIds[$scope.pushedMessageIds.length - 1],
                    function (data) {
                        $scope.warning = (data) ? undefined : "Message not found";
                        $scope.msg = data;
                    },
                    function (data) {
                        $scope.msg = undefined;
                        growlNotifications.add('<h4>Message Lookup Failed</h4>', 'danger', 3000);
                    });
            };

            $scope.loadMessageDetails();

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
