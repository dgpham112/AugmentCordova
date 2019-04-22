//
//  AugmentCordova.m
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AugmentCordova.h"
#import "AugmentARViewController.h"

@implementation AugmentCordova

#pragma mark - Getters

- (AugmentARViewController*) AugmentARViewController {
    UIViewController* VC = self.viewController.presentedViewController;
    if ([VC isKindOfClass: AugmentARViewController.class]) {
        return (AugmentARViewController*) VC;
    }
    
    return nil;
}

#pragma mark - Implementation

/**
 * This method corresponds to `AugmentCordova.init`
 * arguments[0] is a NSDictionary object with @"id" @"key" @"vuforia" keys
 * it may have an optional @"uiElements" keys that is itself a NSDictionary (@see AugmentCordova.config.UIElements)
 */
- (void) initPlugin: (CDVInvokedUrlCommand*) command {
    
    NSDictionary* data = command.arguments[0];
    AugmentCordovaConfig* config = [AugmentCordovaConfig sharedInstance];
    
    config.AppId      = data[ARG_APPID];
    config.AppKey     = data[ARG_APPKEY];
    config.VuforiaKey = data[ARG_VUFORIAKEY];
    
    if (data[ARG_UIELEMENTS] != nil) {
        NSArray* uiElements = data[ARG_UIELEMENTS];
        config.UIElements = [uiElements mutableCopy];
    }
    
    [self sendPluginResultWithSuccess: nil forCommand: command];
}

/**
 * This method corresponds to `AugmentCordova.checkIfModelDoesExistForUserProduct`
 * arguments[0] is a NSDictionary object that represent a product 
 * it has @"identifier" @"brand" @"name" and @"ean" keys
 * This method returns an augmentProduct thru the Cordova callback mechanism
 */
- (void) checkIfModelDoesExistForUserProduct: (CDVInvokedUrlCommand*) command {
    
    AugmentCordovaConfig* config = [AugmentCordovaConfig sharedInstance];
    [AGTAugmentSDK setSharedClientID: config.AppId
                  sharedClientSecret: config.AppKey
                    sharedVuforiaKey: config.VuforiaKey];
    
    id data = command.arguments[0];
    NSDictionary* product = [self buildProductFromArgs:data];
    AGTAugmentSDK* augmentSDK = [AGTAugmentSDK new];
    
    __weak AugmentCordova* weakSelf = self;
    [augmentSDK.productsDataController checkIfModelDoesExistForProductIdentifier: product[ARG_IDENTIFIER] brand: product[ARG_BRAND] name: product[ARG_NAME] EAN: product[ARG_EAN] completion: ^(AGTProduct * _Nullable augmentProduct, NSError * _Nullable error) {
        
        // Check if an error occured
        if (error != nil) {
            [weakSelf sendPluginResultWithErrorMessage: error.localizedDescription forCommand: command];
            return;
        }
        
        // Return the Augment Product in the callback
        [weakSelf sendPluginResultWithSuccess: [weakSelf getDictionaryForProduct: augmentProduct] forCommand: command];
    }];
}

/**
 * This method corresponds to `AugmentCordova.start`
 */
- (void) start: (CDVInvokedUrlCommand*) command {

    AugmentARViewController* augmentARViewController = [[AugmentARViewController alloc] initWithNibName: NSStringFromClass(AugmentARViewController.class) bundle: nil];
    augmentARViewController.webView = self.webView;

    __weak AugmentCordova* weakSelf = self;

    [self.viewController presentViewController: augmentARViewController animated: YES completion: ^{
        // The completion handler is called after the view​Did​Appear:​ method is called on the presented view controller.
        // Return "success" for this command
        [weakSelf sendPluginResultWithSuccess: nil forCommand: command];
    }];
}

/**
 * This method corresponds to `AugmentCordova.addProductToAugmentPlayer`
 * arguments[0] is a NSDictionary object that represent a product
 * it has @"identifier" @"brand" @"name" and @"ean" keys
 * This method needs to be called after the success of `AugmentCordova.start`
 */
- (void) addProductToAugmentPlayer: (CDVInvokedUrlCommand*) command {
    
    // Check if the AugmentARViewController is started
    if (self.AugmentARViewController == nil) {
        [self sendPluginResultWithErrorMessage: @"addProductToAugmentPlayer() must be used after a success call to start()" forCommand: command];
        return;
    }

    id data = command.arguments[0];
    
    // Add the product to the AugmentARViewController ARView
    // it will handle the ARView state
    [self.AugmentARViewController loadProduct: [self buildProductFromArgs: data]];

    // This command return "success" in any case,
    // the error management will be handled (logic and UI) by the AugmentARViewController
    [self sendPluginResultWithSuccess: nil forCommand: command];
}

/**
 * This method corresponds to `AugmentCordova.recenterProducts`
 * This method needs to be called after the success of `AugmentCordova.start`
 */
- (void) recenterProducts: (CDVInvokedUrlCommand*) command {
    
    // Check if the AugmentARViewController is started
    if (self.AugmentARViewController == nil) {
        [self sendPluginResultWithErrorMessage: @"recenterProducts() must be used after a success call to start()" forCommand: command];
        return;
    }
    
    [self.AugmentARViewController recenterProducts];
    [self sendPluginResultWithSuccess: nil forCommand: command];
}

/**
 * This method corresponds to `AugmentCordova.shareScreenshot`
 * This method needs to be called after the success of `AugmentCordova.start`
 */
- (void) shareScreenshot: (CDVInvokedUrlCommand*) command {
    
    // Check if the AugmentARViewController is started
    if (self.AugmentARViewController == nil) {
        [self sendPluginResultWithErrorMessage: @"shareScreenshot() must be used after a success call to start()" forCommand: command];
        return;
    }
    
    [self.AugmentARViewController shareScreenshot];
    [self sendPluginResultWithSuccess: nil forCommand: command];
}

/**
 * This method corresponds to `AugmentCordova.showAlertMessage`
 * arguments[0] is a NSDictionary object with @"title" @"message" @"buttonText" keys
 * This method needs to be called after the success of `AugmentCordova.start`
 */
- (void) showAlertMessage: (CDVInvokedUrlCommand*) command {
    
    // Check if the AugmentARViewController is started
    if (self.AugmentARViewController == nil) {
        [self sendPluginResultWithErrorMessage: @"showAlertMessage() must be used after a success call to start()" forCommand: command];
        return;
    }

    NSDictionary* data = command.arguments[0];

    [self.AugmentARViewController showAlertWithTitle: data[ARG_TITLE] message: data[ARG_MESSAGE] andButtonText: data[ARG_BUTTONTEXT]];
    [self sendPluginResultWithSuccess: nil forCommand: command];
}

/**
 * This method corresponds to `AugmentCordova.stop`
 * This method needs to be called after the success of `AugmentCordova.start`
 */
- (void) stop: (CDVInvokedUrlCommand*) command {

    // Check if the AugmentARViewController is started
    if (self.AugmentARViewController == nil) {
        [self sendPluginResultWithErrorMessage: @"stop() must be used after a success call to start()" forCommand: command];
        return;
    }
    
    __weak AugmentCordova* weakSelf = self;
    [self.viewController dismissViewControllerAnimated: YES completion: ^{
        // Return "success" for this command
        [weakSelf sendPluginResultWithSuccess: nil forCommand: command];
    }];
}

#pragma mark - Helpers

/**
 * Send a valid JSON message for a success command (default message is "ok")
 * {"success": string}
 */
- (void) sendPluginResultWithSuccess: (NSDictionary*) data forCommand: (CDVInvokedUrlCommand*) command {
    
    if (data == nil) {
        data = @{ARG_SUCCESS: @"ok"};
    }
    
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsDictionary: data];
    [self.commandDelegate sendPluginResult: result callbackId: command.callbackId];
}

/**
 * Send a valid JSON error for an error command
 * {"error": string}
 */
- (void) sendPluginResultWithErrorMessage: (NSString*) message forCommand: (CDVInvokedUrlCommand*) command {
    
    NSLog(@"sendPluginResultWithErrorMessage %@", message);
    
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsDictionary:@{
        ARG_ERROR: message
    }];
    [self.commandDelegate sendPluginResult: result callbackId: command.callbackId];
}

/**
 * Convert an AGTProduct object to a NSDictionary
 */
- (NSDictionary*) getDictionaryForProduct: (AGTProduct*) product {
    return @{
        ARG_IDENTIFIER: product.identifier,
        ARG_BRAND:      product.brand,
        ARG_NAME:       product.name,
        ARG_EAN:        product.ean
    };
}

- (NSDictionary*) buildProductFromArgs: (id) data {
    return (NSDictionary*) data;
}

@end
