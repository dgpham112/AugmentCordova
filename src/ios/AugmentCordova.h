//
//  AugmentCordova.h
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import <Cordova/CDV.h>
#import <AugmentPlayerSDK/AugmentPlayerSDK.h>
#import "AugmentCordovaConfig.h"
#import "AugmentARViewController.h"

// Arguments keys

static NSString* ARG_APPID      = @"id";
static NSString* ARG_APPKEY     = @"key";
static NSString* ARG_VUFORIAKEY = @"vuforia";
static NSString* ARG_UIELEMENTS = @"uiElements";
static NSString* ARG_TITLE      = @"title";
static NSString* ARG_MESSAGE    = @"message";
static NSString* ARG_BUTTONTEXT = @"buttonText";
static NSString* ARG_IDENTIFIER = @"identifier";
static NSString* ARG_BRAND      = @"brand";
static NSString* ARG_NAME       = @"name";
static NSString* ARG_EAN        = @"ean";
static NSString* ARG_CODE       = @"code";

// Results keys

static NSString* ARG_ERROR      = @"error";
static NSString* ARG_SUCCESS    = @"success";


@class AugmentARViewController;

@interface AugmentCordova : CDVPlugin

- (AugmentARViewController*) AugmentARViewController;

- (void) initPlugin: (CDVInvokedUrlCommand*) command;
- (void) checkIfModelDoesExistForUserProduct: (CDVInvokedUrlCommand*) command;
- (void) start: (CDVInvokedUrlCommand*) command;
- (void) addProductToAugmentPlayer: (CDVInvokedUrlCommand*) command;
- (void) recenterProducts: (CDVInvokedUrlCommand*) command;
- (void) shareScreenshot: (CDVInvokedUrlCommand*) command;
- (void) showAlertMessage: (CDVInvokedUrlCommand*) command;
- (void) stop: (CDVInvokedUrlCommand*) command;

@end
