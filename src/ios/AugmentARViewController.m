//
//  AugmentARViewController.m
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import "AugmentARViewController.h"
#import <WebKit/WebKit.h>

@implementation AugmentARViewController

#pragma mark - UIViewController life cycle

- (void) viewDidLoad {
    [super viewDidLoad];
    
    // Init variables
    self.progresses = [NSMutableArray new];
    
    // Configure views
    self.tutorialScrollview.delegate = self;
    [self.closeButton addTarget: self action: @selector(closeButtonAction:) forControlEvents: UIControlEventTouchUpInside];
    [self.helpButton addTarget: self action: @selector(helpButtonAction:) forControlEvents: UIControlEventTouchUpInside];
    [self.tutorialGotItButton addTarget: self action: @selector(gotItButtonAction:) forControlEvents: UIControlEventTouchUpInside];
    
    // Init Augment SDK
    AugmentCordovaConfig* config = [AugmentCordovaConfig sharedInstance];
    [AGTAugmentSDK setSharedClientID: config.AppId
                  sharedClientSecret: config.AppKey
                    sharedVuforiaKey: config.VuforiaKey];
    
    self.augmentSDK = [AGTAugmentSDK new];
    [self.augmentView setAugmentPlayer: self.augmentSDK.augmentPlayer];
    
    [self addUIElements];
    [self prepareARSession];
}

- (void) viewWillDisappear: (BOOL) animated {
    if (self.augmentSDK != nil) {
        [self.augmentSDK.augmentPlayer pause];
    }
}

- (void) viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    // Final frame for our AugmentCordovaButton (if exists)
    CGFloat count = (CGFloat) self.toolbarContainer.subviews.count;
    if (count <= 0) return;
    
    CGFloat margin = 4;
    CGFloat cpt    = 0;
    CGFloat width  = (self.view.frame.size.width - margin * (count+1) * 2) / count;
    CGFloat height = 48;
    if (self.traitCollection.horizontalSizeClass == UIUserInterfaceSizeClassRegular) {
        height = 60;
    }
    
    CGRect currentFrame = self.toolbarContainer.frame;
    currentFrame.size.height = height;
    currentFrame.origin.y = currentFrame.origin.y + 48 /* Interface builder fixed size */ - height;
    self.toolbarContainer.frame = currentFrame;

    for (AugmentCordovaButton* view in self.toolbarContainer.subviews) {
        
        [view setFrame: CGRectMake(
            margin * 2 + cpt * width + margin * cpt * 2,
            -2 * margin,
            width,
            height
        )];
        cpt++;
    }
}

#pragma mark - UI Elements and Actions

/**
 * Create the user defined button with associated javascript actions
 * @see AugmentCordovaConfig.UIElements for more details
 */
- (void) addUIElements {
    AugmentCordovaConfig* config = [AugmentCordovaConfig sharedInstance];
    NSArray* uiElements = config.UIElements;
    
    for (NSDictionary* element in uiElements) {
        AugmentCordovaButton* button = [AugmentCordovaButton new];
        [button applyData: element];
        [button addTarget: self action: @selector(buttonAction:) forControlEvents: UIControlEventTouchUpInside];
        [self.toolbarContainer addSubview: button];
    }
}

- (void) buttonAction: (AugmentCordovaButton*) sender {
    
    if ([self.webView isKindOfClass: UIWebView.class]) {
        [((UIWebView*) self.webView) stringByEvaluatingJavaScriptFromString: sender.code];
    }
    else if ([self.webView isKindOfClass: WKWebView.class]) {
        [((WKWebView*) self.webView) evaluateJavaScript: sender.code completionHandler: nil];
    }
}

- (void) gotItButtonAction: (UIButton*) sender {
    [self tutorialHide];
}

- (void) helpButtonAction: (UIButton*) sender {
    [self tutorialShow];
}

- (void) closeButtonAction: (UIButton*) sender {
    [self dismissViewControllerAnimated: YES completion: nil];
}

- (void) tutorialHide {
    self.tutorialContainer.hidden = YES;
    self.toolbarContainer.hidden  = NO;
    // Got back to first page
    [self.tutorialScrollview scrollRectToVisible: CGRectMake(0, 0, 1, 1) animated: NO];
    self.tutorialPageControl.currentPage = 0;
    self.tutorialGotItButton.hidden = YES;
}

- (void) tutorialShow {
    self.tutorialContainer.hidden = NO;
    self.toolbarContainer.hidden  = YES;
}

#pragma mark - ARView logic

- (void) prepareARSession {
    self.loadingContainerView.hidden = NO;
    
    /**
     * We need to grant camera permission before starting AugmentPlayerSDK
     * otherwise we will have an error during initialization: "Cannot access the camera"
     * Also we must update our Info.plist to allow camera access on the App level, key: NSCameraUsageDescription
     */
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType: AVMediaTypeVideo];
    if (authStatus == AVAuthorizationStatusAuthorized) {
        // Good :)
    }
    else if (authStatus == AVAuthorizationStatusNotDetermined) {
        // Ask?
        __weak AugmentARViewController* weakSelf = self;
        [AVCaptureDevice requestAccessForMediaType: AVMediaTypeVideo completionHandler: ^(BOOL granted) {
            if (!granted) {
                // Bad :(
                [weakSelf showError: @"Augment Player need access to your camera to work.\nGo to Setting and allow access for this App."];
            }
        }];
    }
    else {
        // Bad :(
        [self showError: @"Augment Player need access to your camera to work.\nGo to Setting and allow access for this App."];
    }
}

/**
 * Load will get the product from Augment (or from the cache if available)
 */
- (void) loadProduct: (NSDictionary*) product {
    // Check if the product is already in cache
    AGTProduct* cachedProduct = [self.augmentSDK.productsDataController productForIdentifier: product[@"identifier"]];
    if (cachedProduct != nil) {
            
        // No product found in Augment Product Database
        if (cachedProduct == AGTProduct.unfoundProduct) {
            self.loadingContainerView.hidden = YES;
            [self showError: @"This product is not available yet"];
            return;
        }
        
        // Add the product to the augmented view
        [self addToPlayer: cachedProduct];
    }
    else {
        // Query Augment Product Database
        [self queryAugmentDatabase: product];
    }
}

- (void) queryAugmentDatabase: (NSDictionary*) product {
    __weak AugmentARViewController* weakSelf = self;
    [self.augmentSDK.productsDataController checkIfModelDoesExistForProductIdentifier: product[@"identifier"] brand: product[@"brand"] name: product[@"name"] EAN: product[@"ean"] completion: ^(AGTProduct* _Nullable augmentProduct, NSError* _Nullable error) {

        // Check if an error occured
        if (error != nil) {
            weakSelf.loadingContainerView.hidden = YES;
            [weakSelf showError: error.localizedDescription];
            return;
        }
        
        // Check if the product is found
        if (augmentProduct != nil) {
            [weakSelf addToPlayer: augmentProduct];
        }
        else {
            weakSelf.loadingContainerView.hidden = YES;
            [weakSelf showError: @"This product is not available yet"];
        }
    }];
}

/**
 * Add the Augment-product to the AR View
 * In this process the 3D model will be downloaded from the network
 *
 * @param augmentProduct AGTProduct The Augment product from the API call
 */
- (void) addToPlayer: (AGTProduct*) augmentProduct {
    __weak AugmentARViewController* weakSelf = self;
    __block int progressCount = (int) self.progresses.count;
    [self.progresses addObject: @0];
    [self.augmentSDK addProductToAugmentPlayer: augmentProduct downloadProgress: ^(NSProgress* _Nonnull progress) {
        // Progress callback
        dispatch_async(dispatch_get_main_queue(), ^{
            NSLog(@"progress %@", progress);
            weakSelf.progresses[progressCount] = @(progress.fractionCompleted * 100);
            float total = 0;
            for (NSNumber* c in weakSelf.progresses) {
                total += c.intValue;
            }
            total = total / weakSelf.progresses.count;
            weakSelf.loadingLabel.text = [NSString stringWithFormat:@"%i %% completed", (int) round(total)];
        });
    } operationCompletionWithModelIdentifier: ^(NSUUID* _Nullable itemIdentifier, NSArray<NSError*>* _Nullable errors) {
        
        // Check for errors
        if (errors != nil) {
            NSMutableString* errorString = [NSMutableString new];
            [errors enumerateObjectsUsingBlock: ^(NSError* _Nonnull obj, NSUInteger idx, BOOL* _Nonnull stop) {
                [errorString appendString: obj.localizedDescription];
                [errorString appendString: @" "];
            }];
            [weakSelf showError: errorString];
            return;
        }
        
        // If everything is ok we start rendering the model
        if (itemIdentifier != nil) {
            [weakSelf startAR];
        }
        else {
            weakSelf.loadingContainerView.hidden = YES;
            [weakSelf showError: @"Impossible to show this product yet"];
        }
    }];
}

- (void) startAR {
    self.loadingContainerView.hidden = YES;
    [self.augmentSDK.augmentPlayer resume];
}

#pragma mark - Javascript methods on ARView

- (void) recenterProducts {
    [self.augmentSDK.augmentPlayer recenterProducts];
}

- (void) shareScreenshot {
    __weak AugmentARViewController* weakSelf = self;
    [self.augmentSDK.augmentPlayer takeScreenshotWithCompletion:^(UIImage * _Nullable screenshotImage) {
        if (screenshotImage != nil) {
            [weakSelf shareImage: screenshotImage fromView: weakSelf.view];
        }
    }];
}

/**
 * This will allow external developers to give feedback to their user in the AugmentARViewController
 */
- (void) showAlertWithTitle: (NSString*) title message: (NSString*) message andButtonText: (NSString*) buttonText {
    __weak AugmentARViewController* weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertController* alert = [UIAlertController alertControllerWithTitle: title message: message preferredStyle: UIAlertControllerStyleAlert];
        [alert addAction: [UIAlertAction actionWithTitle: buttonText style: UIAlertActionStyleDefault handler: nil]];
        [weakSelf presentViewController: alert animated: YES completion: nil];
    });
}

#pragma mark - UISCrollViewDelegate

- (void) scrollViewDidEndDecelerating: (UIScrollView*) scrollView {
    int pageCount  = (int) self.tutorialPageControl.numberOfPages;
    int pageNumber = (int) round(scrollView.contentOffset.x / scrollView.frame.size.width);
    self.tutorialPageControl.currentPage = pageNumber;
    self.tutorialGotItButton.hidden = pageNumber < pageCount - 1;
}

#pragma mark - Helpers

- (void) shareImage: (UIImage*) image fromView:(UIView*) view {
    __weak AugmentARViewController* weakSelf = self;
    NSArray* shareItems = @[image];
    UIActivityViewController* shareViewController = [[UIActivityViewController alloc] initWithActivityItems:shareItems applicationActivities:nil];
    shareViewController.completionWithItemsHandler = ^ (UIActivityType __nullable activityType, BOOL completed, NSArray * __nullable returnedItems, NSError * __nullable activityError) {
        if (activityError != nil) {
            [weakSelf showError: activityError.localizedDescription];
        }
    };
    if (shareViewController.popoverPresentationController != nil) {
        shareViewController.popoverPresentationController.sourceView = view;
    }
    [self presentViewController: shareViewController animated: YES completion: nil];
}

- (void) showError: (NSString*) message {
    [self showAlertWithTitle: @"Error" message: message andButtonText: @"OK"];
}

@end
