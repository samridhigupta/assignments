/**
 * Created by samridhi on 04/10/15.
 */
(function() {

    // our app definition
    var reproApp = angular.module('reproApp', [
        'ngRoute',
        'ngCookies',
        'ngSanitize',
        'reproControllers'
    ]);

    //-- Our routing definition
    reproApp.config(['$routeProvider',

        function ($routeProvider) {

            $routeProvider.
                when('/readme', {
                    templateUrl: 'views/readme.html'
                }).
                when('/order/new', {
                    templateUrl: 'views/order.html',
                    controller: 'orderController',
                }).
                when('/order/edit', {
                    templateUrl: 'views/orderedit.html',
                    controller: 'orderEditController'
                }).
                otherwise({
                    redirectTo: '/readme'
                });
        }
    ]);

    // reference the controller module
    var reproControllers = angular.module('reproControllers', []);

    //-- A custom directive, textModel
    reproControllers.directive(
        'textModel', ['$interval', function ($interval) {

            function link(scope, element, attrs) {

                // reference
                var $element = $(element);

                // update the value on screen
                function updateControl(newValue, force) {

                    if (force || ($element.val() !== newValue + "")) {

                        $element.val((newValue === undefined ? "" : newValue) + "");
                    }
                }

                // setup control
                $element.
                    on("blur paste select" + ($element.is("select") ? " change keyup" : ""), function (e) {

                        setTimeout(function () {

                            var oldValue = scope.$eval(attrs.textModel);
                            var newValue = $element.val();

                            updateControl(newValue);

                            scope.$apply(function () {

                                if (oldValue != newValue) {

// ==>
                                    // this is where the value get's set. The problem is, that when using route #/order/edit instead of #/order/new the scope is not the
                                    // one, I expected, and the $watch of the orderController is not fired.
                                    scope.$eval(attrs.textModel + ' = value;', { value: newValue });

                                    if (attrs.modelChange) try { scope.$eval(attrs.modelChange, { newValue: newValue, oldValue: oldValue }); } catch (e) { }
                                }
                            });

                        }, 1);
                    }
                );

                updateControl(scope.$eval(attrs.textModel), true);

                // update screen when model changes
                scope.$watch(attrs.textModel, updateControl);
                if (attrs.watchModel) {

                    scope.$watch(attrs.watchModel, function () {

                        setTimeout(function () {

                            updateControl(scope.$eval(attrs.textModel), true);

                        }, 1);
                    });
                }
            }

            return {
                link: link
            };
        }]);


    //-- Order edit controller
    reproControllers.controller(
        'orderEditController',
        ['$scope', '$routeParams', '$rootScope', '$location',

            function ($scope, $routeParams, $rootScope, $location) {

                // normally we would load an order here, investigate it, and determine what view to enable
                // let's simulate the ajax laoding, using a timer
                setTimeout(function() {

                    $scope.$apply(function() {

                        $scope.orderEdit = true;
                        $scope.enableOrderView = true;
                    });
                }, 500);
            }]
    );

    //-- Normal order controller
    reproControllers.controller(
        'orderController',
        ['$scope', '$routeParams', '$rootScope', '$location',

            function ($scope, $routeParams, $rootScope, $location) {

                // init
                $scope.quickAddressLeft = -1;
                $scope.quickAddressRight = -1;

// ==>
                // this functions, should notice any changes made by the select control, with it's textModel attribute.

                $scope.$watch("quickAddressLeft", function (newValue) {

                    if (newValue != "-1") {
                        alert("You selected option #" + newValue);
                    }
                });
            }]
    );
})();
