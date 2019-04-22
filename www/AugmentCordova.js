'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var exec = require('cordova/exec');

var AugmentCordovaClass = function () {
    function AugmentCordovaClass() {
        _classCallCheck(this, AugmentCordovaClass);
    }

    _createClass(AugmentCordovaClass, [{
        key: 'init',


        // AugmentCordova

        value: function init(credentials) {
            var errors = AugmentCordovaClass.checkMandatoryFields(["id", "key"], credentials);
            if (errors) {
                throw "AugmentCordovaInit " + errors.join(",");
            }

            if (credentials['uiElements']) {
                credentials['uiElements'] = AugmentCordovaClass.normalizeUIElements(credentials['uiElements']);
            }
            AugmentCordovaClass.execAugment('initPlugin', credentials);
        }
    }, {
        key: 'checkIfModelDoesExistForUserProduct',
        value: function checkIfModelDoesExistForUserProduct(product, productCallback, errorCallback) {
            product = AugmentCordovaClass.normalizeProduct(product);
            AugmentCordovaClass.execAugment('checkIfModelDoesExistForUserProduct', product, productCallback, errorCallback);
        }
    }, {
        key: 'startPlayer',
        value: function startPlayer(playerCallback, errorCallback) {
            var _player = new AugmentCordovaPlayer();
            this._player = _player;
            AugmentCordovaClass.execAugment('start', null, function (success) {
                playerCallback(_player);
            }, errorCallback);
        }
    }, {
        key: 'player',
        get: function get() {
            if (!this._player) {
                throw "You must call AugmentCordova.startPlayer first.";
            }
            return this._player;
        },
        set: function set(value) {
            this._player = value;
        }

        // Default callbacks

    }], [{
        key: 'PluginClassName',
        value: function PluginClassName() {
            return 'AugmentCordova';
        }
    }, {
        key: 'defaultErrorCallback',
        value: function defaultErrorCallback(error) {
            console.error(AugmentCordovaClass.PluginClassName() + ' ERROR!');
            console.error(error);
        }
    }, {
        key: 'defaultSuccessCallback',
        value: function defaultSuccessCallback(success) {
            console.log(AugmentCordovaClass.PluginClassName() + ': SUCCESS!');
            console.log(success);
        }

        // Helpers

    }, {
        key: 'checkMandatoryFields',
        value: function checkMandatoryFields(fields, onObject) {
            var errors = [];
            var _iteratorNormalCompletion = true;
            var _didIteratorError = false;
            var _iteratorError = undefined;

            try {
                for (var _iterator = fields[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                    var field = _step.value;

                    if (typeof onObject[field] === "undefined") {
                        errors.push("missing mandatory field " + field);
                    }
                }
            } catch (err) {
                _didIteratorError = true;
                _iteratorError = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion && _iterator.return) {
                        _iterator.return();
                    }
                } finally {
                    if (_didIteratorError) {
                        throw _iteratorError;
                    }
                }
            }

            if (errors.length == 0) {
                return null;
            }

            return errors;
        }

        // Warning no UI actions are possible with javascript on the Android version
        // `alert` and other code that shows dialogs will not pop over the ARView (but will pop under)
        // This will work on iOS but it is better if you don't use it
        // Use `AugmentCordova.showAlertMessage` instead

    }, {
        key: 'normalizeUIElements',
        value: function normalizeUIElements(actions) {
            var data = [];
            var _iteratorNormalCompletion2 = true;
            var _didIteratorError2 = false;
            var _iteratorError2 = undefined;

            try {
                for (var _iterator2 = actions[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
                    var action = _step2.value;

                    var errors = AugmentCordovaClass.checkMandatoryFields(["title", "code"], action);
                    if (errors) {
                        throw "UIElements " + errors.join(",");
                    }

                    action["code"] = "(" + action["code"].toString() + ")(AugmentCordova.player)";
                    data.push(action);
                }
            } catch (err) {
                _didIteratorError2 = true;
                _iteratorError2 = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion2 && _iterator2.return) {
                        _iterator2.return();
                    }
                } finally {
                    if (_didIteratorError2) {
                        throw _iteratorError2;
                    }
                }
            }

            return data;
        }
    }, {
        key: 'normalizeProduct',
        value: function normalizeProduct(product) {
            var errors = AugmentCordovaClass.checkMandatoryFields(["brand", "name", "identifier"], product);
            if (errors) {
                throw "AugmentCordovaProduct " + errors.join(",");
            }

            product.ean = product.ean || "";
            return product;
        }
    }, {
        key: 'execAugment',
        value: function execAugment(methodName, args, successCallback, errorCallback) {
            exec(successCallback || AugmentCordovaClass.defaultSuccessCallback, errorCallback || AugmentCordovaClass.defaultErrorCallback, AugmentCordovaClass.PluginClassName(), methodName, [args]);
        }
    }]);

    return AugmentCordovaClass;
}();

var AugmentCordovaPlayer = function () {
    function AugmentCordovaPlayer() {
        _classCallCheck(this, AugmentCordovaPlayer);
    }

    _createClass(AugmentCordovaPlayer, [{
        key: 'addProduct',
        value: function addProduct(product) {
            product = AugmentCordovaClass.normalizeProduct(product);
            AugmentCordovaClass.execAugment('addProductToAugmentPlayer', product);
        }
    }, {
        key: 'recenterProducts',
        value: function recenterProducts() {
            AugmentCordovaClass.execAugment('recenterProducts', null);
        }
    }, {
        key: 'shareScreenshot',
        value: function shareScreenshot() {
            AugmentCordovaClass.execAugment('shareScreenshot', null);
        }
    }, {
        key: 'showAlertMessage',
        value: function showAlertMessage(title, message, buttonText) {
            var data = {
                title: title || "Error",
                message: message || "",
                buttonText: buttonText || "OK"
            };
            AugmentCordovaClass.execAugment('showAlertMessage', data);
        }
    }, {
        key: 'stop',
        value: function stop(successCallback) {
            AugmentCordovaClass.execAugment('stop', null, successCallback);
        }
    }]);

    return AugmentCordovaPlayer;
}();

// Export module

module.exports = new AugmentCordovaClass();