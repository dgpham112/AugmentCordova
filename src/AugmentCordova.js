// @flow
var exec = require('cordova/exec');

declare type AugmentCordovaProduct = {
    identifier: string,
    brand: string,
    name: string,
    ean?: string
}

declare type AugmentCordovaSuccess = {
    success: any
};

declare type AugmentCordovaError = {
    error: string
};

declare type AugmentCordovaInit = {
    id: string,
    key: string,
    vuforia: string,
    uiElements?: AugmentCordovaButton[]
}

declare type AugmentCordovaAction = string | (player: AugmentCordovaPlayer) => void;

declare type AugmentCordovaButton = {
    title: string,
    code: AugmentCordovaAction,
    // TODO
    //image?: string,
    //style?: {[key: string] : string}
}

declare type AugmentCordovaSuccessCallback = (success: AugmentCordovaSuccess) => void;
declare type AugmentCordovaProductCallback = (product: ?AugmentCordovaProduct) => void;
declare type AugmentCordovaPlayerCallback  = (player: AugmentCordovaPlayer) => void;
declare type AugmentCordovaErrorCallback   = (error: AugmentCordovaError) => void;

class AugmentCordovaClass {

    _player: AugmentCordovaPlayer;

    static PluginClassName() : string {
        return 'AugmentCordova';
    }

    get player() : AugmentCordovaPlayer {
        if (!this._player) {
            throw "You must call AugmentCordova.startPlayer first.";
        }
        return this._player;
    }

    set player(value: AugmentCordovaPlayer) : void {
        this._player = value;
    }

    // Default callbacks

    static defaultErrorCallback(error: AugmentCordovaError) {
        console.error(AugmentCordovaClass.PluginClassName() + ' ERROR!');
        console.error(error);
    }

    static defaultSuccessCallback(success: AugmentCordovaSuccess) {
        console.log(AugmentCordovaClass.PluginClassName() + ': SUCCESS!');
        console.log(success);
    }

    // Helpers

    static checkMandatoryFields(fields: string[], onObject: any) : string[] | null {
        var errors: string[] = [];
        for (var field of fields) {
            if (typeof onObject[field] === "undefined") {
                errors.push("missing mandatory field " + field);
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
    static normalizeUIElements(actions: AugmentCordovaButton[]) {
        var data = [];
        for (var action of actions) {
            let errors = AugmentCordovaClass.checkMandatoryFields(["title", "code"], action);
            if (errors) {
                throw "UIElements " + errors.join(",");
            }

            action["code"] = "(" + action["code"].toString() + ")(AugmentCordova.player)";
            data.push(action);
        }

        return data;
    }

    static normalizeProduct(product: AugmentCordovaProduct) : AugmentCordovaProduct {
        let errors = AugmentCordovaClass.checkMandatoryFields(["brand", "name", "identifier"], product);
        if (errors) {
            throw "AugmentCordovaProduct " + errors.join(",");
        }

        product.ean = product.ean || "";
        return product;
    }

    static execAugment(methodName: string, args: any, successCallback: ?AugmentCordovaSuccessCallback, errorCallback: ?AugmentCordovaErrorCallback) {
        exec(
            successCallback || AugmentCordovaClass.defaultSuccessCallback,
            errorCallback   || AugmentCordovaClass.defaultErrorCallback,
            AugmentCordovaClass.PluginClassName(),
            methodName,
            [args]
        );
    }

    // AugmentCordova

    init(credentials: AugmentCordovaInit) {
        let errors = AugmentCordovaClass.checkMandatoryFields(["id", "key"], credentials);
        if (errors) {
            throw "AugmentCordovaInit " + errors.join(",");
        }

        if (credentials['uiElements']) {
            credentials['uiElements'] = AugmentCordovaClass.normalizeUIElements(credentials['uiElements']);
        }
        AugmentCordovaClass.execAugment('initPlugin', credentials);
    }

    checkIfModelDoesExistForUserProduct(product: AugmentCordovaProduct, productCallback: AugmentCordovaProductCallback, errorCallback: AugmentCordovaErrorCallback) {
        product = AugmentCordovaClass.normalizeProduct(product);
        AugmentCordovaClass.execAugment('checkIfModelDoesExistForUserProduct', product, productCallback, errorCallback);
    }

    startPlayer(playerCallback: AugmentCordovaPlayerCallback, errorCallback: AugmentCordovaErrorCallback) {
        let _player = new AugmentCordovaPlayer();
        this._player = _player;
        AugmentCordovaClass.execAugment('start', null, function (success) {
            playerCallback(_player);
        }, errorCallback);
    }
}

class AugmentCordovaPlayer {

    addProduct(product: AugmentCordovaProduct) {
        product = AugmentCordovaClass.normalizeProduct(product);
        AugmentCordovaClass.execAugment('addProductToAugmentPlayer', product);
    }

    recenterProducts() {
        AugmentCordovaClass.execAugment('recenterProducts', null);
    }

    shareScreenshot() {
        AugmentCordovaClass.execAugment('shareScreenshot', null);
    }

    showAlertMessage(title: string, message: string, buttonText: string) {
        var data = {
            title:      title      || "Error",
            message:    message    || "",
            buttonText: buttonText || "OK"
        };
        AugmentCordovaClass.execAugment('showAlertMessage', data);
    }

    stop(successCallback: AugmentCordovaSuccessCallback) {
        AugmentCordovaClass.execAugment('stop', null, successCallback);
    }
}

// Export module

module.exports = new AugmentCordovaClass();
