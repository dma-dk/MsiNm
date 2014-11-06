/**
 * User-related controllers
 */

angular.module('msinm.user')

    /**
     * The UserCtrl handles login and registration of new users
     */
    .controller('UserCtrl', ['$scope', '$rootScope', '$cookieStore', '$modal', 'UserService',
        function ($scope, $rootScope, $cookieStore, $modal, UserService) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = { email:$cookieStore.get('lastLogin'), password: undefined };

        $scope.newUser = {};

        $scope.viewMode = "login";

        $scope.$on("$destroy", function() {
            delete $rootScope.loginDialog;
        });

        $scope.login = function() {

            UserService.authenticate(
                $scope.user,
                function(data) {
                    $cookieStore.put('lastLogin', $scope.user.email);
                    console.log("SUCCESS");
                    $scope.$close();
                },
                function(data, status) {
                    console.log("ERROR");
                    $scope.user.password = undefined;
                    $scope.error = "Error in name or password";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

        $scope.setViewMode = function (viewMode) {
            $scope.viewMode = viewMode;
        };

        $scope.resetPassword = function () {
            $scope.message = "Resetting password...";
            $scope.viewMode = "info";

            UserService.resetPassword(
                $scope.user.email,
                function(data) {
                    $scope.error = undefined;
                    $scope.message = "An email has been sent to " + $scope.user.email
                        + ". Please follow the instructions to reset your password.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the email address.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        };

        $scope.registerDlg = function() {
            // Require IE 11 or newer
            if (isIE() < 11) {
                alert("For this prototype, please use IE version 11 or newer.\nOr use a new'ish version of Chrome, Firefox or Safari");
                return;
            }

            $modal.open({
                controller: "UserDetailsCtrl",
                templateUrl : "/partials/user/user-details-dialog.html",
                resolve: {
                    user: function(){ return {}; },
                    userAction: function(){ return 'register'; }
                }
            });
        };

        $scope.updateUserDlg = function() {
            UserService.currentUser(
                function (data) {
                    $modal.open({
                        controller: "UserDetailsCtrl",
                        templateUrl : "/partials/user/user-details-dialog.html",
                        resolve: {
                            user: function(){ return data; },
                            userAction: function(){ return 'edit'; }
                        }
                    });
                },
                function (data) {
                    console.log("Error getting current user details")
                }
            );
        };


        $scope.mailListsDlg = function () {
            $modal.open({
                controller: "UserMailingListCtrl",
                templateUrl : "/partials/user/user-mailing-list-dialog.html"
            });
        };


        $scope.checkLoggedIn = function(path) {
            if ($rootScope.currentUser) {
                $rootScope.go(path);
            } else {
                $rootScope.$broadcast('Login', "Please login first");
            }

        }

    }])

    /**
     * The UserDetailsCtrl handles registration and updating of users
     */
    .controller('UserDetailsCtrl', ['$scope', 'UserService', 'userAction', 'user',
        function ($scope, UserService, userAction, user) {
            'use strict';

            $scope.focusMe = true;
            $scope.message = undefined;
            $scope.error = undefined;
            $scope.viewMode = 'edit';
            $scope.userAction = userAction;
            $scope.user = user;

            $scope.register = function() {
                UserService.registerUser(
                    $scope.user.email,
                    $scope.user.firstName,
                    $scope.user.lastName,
                    $scope.user.language,
                    $scope.user.mmsi,
                    $scope.user.vesselName,
                    function(data) {
                        console.log("User " + $scope.user.email + " registered");
                        $scope.message = $scope.user.email + " has been registered as a new user. An activation email has been sent to you.";
                        $scope.viewMode = "info";
                        $scope.error = undefined;
                    },
                    function(data) {
                        $scope.error = "An error happened: " + data + ".";
                    });
            };

            $scope.update = function() {
                UserService.updateCurrentUser(
                    $scope.user.email,
                    $scope.user.firstName,
                    $scope.user.lastName,
                    $scope.user.language,
                    $scope.user.mmsi,
                    $scope.user.vesselName,
                    function(data) {
                        $scope.$dismiss('closed');
                    },
                    function(data) {
                        $scope.error = "An error happened: " + data + ".";
                    });
            };
        }])


    /**
     * The UserMailingListCtrl handles mailing list subscription for the current  user
     */
    .controller('UserMailingListCtrl', ['$scope', 'MailingListService', 'DialogService',
        function ($scope, MailingListService, DialogService) {
            'use strict';

            $scope.error = undefined;
            $scope.mailingLists = [];

            $scope.loadMailingLists = function () {
                MailingListService.getUserMailingLists(
                    function(data) {
                        $scope.mailingLists = data;
                    },
                    function(data) {
                        $scope.error = "Error: " + data + ".";
                    }
                );
            };

            $scope.update = function() {
                var mailListIds = [];
                for (var m in $scope.mailingLists) {
                    if ($scope.mailingLists[m].selected) {
                        mailListIds.push($scope.mailingLists[m].id);
                    }
                }
                MailingListService.updateUserSubscription(
                    mailListIds,
                    function(data) {
                        $scope.$dismiss('closed');
                    },
                    function(data) {
                        $scope.error = "Error: " + data + ".";
                    }
                );
            };

            $scope.canDelete = function (mailList) {
                return mailList.user == $scope.currentUser.email || $scope.hasRole('sysadmin');
            };

            $scope.deleteMailingList = function (mailList) {
                DialogService.showConfirmDialog(
                    "Delete Mailing List?", "Delete mailing list '" + mailList.name + "'?")
                    .then(function() {
                        MailingListService.deleteMailingList(
                            mailList.id,
                            function(data) {
                                $scope.loadMailingLists();
                            },
                            function(data) {
                                $scope.error = "Error: " + data + ".";
                            }
                        );
                    });
            }
        }])


    /**
     * The NewMailingListCtrl is used for creating a new mailing list base on search filter parameters
     */
    .controller('NewMailingListCtrl', ['$scope', '$modal', '$timeout', 'MailingListService', 'filterParams',
        function ($scope, $modal, $timeout, MailingListService, filterParams) {
            'use strict';

            $scope.focusMe = true;
            $scope.filterParams = filterParams;
            $scope.error = undefined;
            $scope.mailList = {};
            $scope.templates = [];

            $scope.newMailingListTemplate = function () {
                MailingListService.newMailingListTemplate(
                    $scope.filterParams,
                    function(data) {
                        $scope.mailList = data;
                    },
                    function(data) {
                        $scope.error = "Error: " + data + ".";
                    }
                );

                $timeout(function () {
                    MailingListService.getUserMailingListTemplates(
                        function(data) {
                            $scope.templates = data;
                            if (!$scope.mailList.template && $scope.templates.length > 0) {
                                $scope.mailList.template = $scope.templates[0];
                            }
                        },
                        function(data) {
                            $scope.error = "Error: " + data + ".";
                        }
                    );
                }, 100);
            };

            $scope.save = function() {
                MailingListService.createMailingList(
                    $scope.mailList,
                    function(data) {
                        $scope.$dismiss('closed');
                    },
                    function(data) {
                        $scope.error = "Error: " + data + ".";
                    }
                );
            };
        }])


    /**
     * The NewPasswordCtrl handles setting a new password
     */
    .controller('NewPasswordCtrl', ['$scope', '$location', 'UserService', 'email', 'token',
        function ($scope, $location, UserService, email, token) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = { email:email, password: undefined, password2: undefined };
        $scope.token = token;
        $scope.viewMode = "new-pwd";

        $scope.$on("$destroy", function() {
            $location.path("/");
        });

        $scope.updatePassword = function() {
            UserService.updatePassword(
                $scope.user.email,
                $scope.user.password,
                $scope.token,
                function(data) {
                    $scope.error = undefined;
                    $scope.message = "The password has been updated.";
                    $scope.viewMode = "info";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the email address.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        }

    }])


    /**
     * The AddOrEditUserCtrl handles adding or editing a user by an administrator
     */
    .controller('AddOrEditUserCtrl', ['$scope', '$modalInstance', 'UserService', 'user', 'userAction',
        function ($scope, $modalInstance, UserService, user, userAction) {
        'use strict';

        $scope.focusMe = true;
        $scope.message = undefined;
        $scope.error = undefined;
        $scope.user = user;
        $scope.roles = {
            user: true,
            editor: $.inArray('editor', user.roles) > -1,
            admin: $.inArray('admin', user.roles) > -1,
            sysadmin: $.inArray('sysadmin', user.roles) > -1
        };
        $scope.userAction = userAction;

        $scope.createOrUpdateUser = function() {
            var roles = [];
            if ($scope.roles.user) {
                roles.push("user");
            }
            if ($scope.roles.editor) {
                roles.push("editor");
            }
            if ($scope.roles.admin) {
                roles.push("admin");
            }
            if ($scope.roles.sysadmin) {
                roles.push("sysadmin");
            }
            UserService.createOrUpdateUser(
                $scope.user.email,
                $scope.user.firstName,
                $scope.user.lastName,
                $scope.user.language,
                roles,
                $scope.user.activationEmail,
                function(data) {
                    $modalInstance.close();
                },
                function(data) {
                    $scope.error = "An error happened. " + data + "Please check the email address.";
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                });
        }

    }]);




