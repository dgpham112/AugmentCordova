//
//  AugmentARViewController.h
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <AugmentPlayerSDK/AugmentPlayerSDK.h>
#import <AugmentPlayerSDK/AGTAugmentPlayer+Private.h>
#import <Cordova/CDVViewController.h>
#import "AugmentCordova.h"
#import "AugmentCordovaButton.h"

@class CDVViewController;

@interface AugmentARViewController : UIViewController <UIScrollViewDelegate>

#pragma mark - User Interface

@property (weak, nonatomic) IBOutlet AGTView* augmentView;

// Loading UI
@property (weak, nonatomic) IBOutlet UIView* loadingContainerView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *loadingActivityIndicator;
@property (weak, nonatomic) IBOutlet UILabel* loadingLabel;

// Tutorial UI
@property (weak, nonatomic) IBOutlet UIView* tutorialContainer;
@property (weak, nonatomic) IBOutlet UIScrollView* tutorialScrollview;
@property (weak, nonatomic) IBOutlet UIPageControl* tutorialPageControl;
@property (weak, nonatomic) IBOutlet UIButton* tutorialGotItButton;

@property (weak, nonatomic) IBOutlet UIButton *closeButton;
@property (weak, nonatomic) IBOutlet UIButton* helpButton;

// User defined button container
@property (weak, nonatomic) IBOutlet UIView* toolbarContainer;

#pragma mark - Properties

@property (nonatomic, strong) AGTAugmentSDK *augmentSDK;

// Reference to the Cordova webview so we can execute Javascript on it
@property (nonatomic, weak) UIView *webView;

@property (strong, nonatomic) NSMutableArray<NSNumber*>* progresses;

#pragma mark - Public methods

- (void) prepareARSession;
- (void) recenterProducts;
- (void) shareScreenshot;
- (void) showAlertWithTitle: (NSString*) title message: (NSString*) message andButtonText: (NSString*) buttonText;
- (void) loadProduct: (NSDictionary*) product;
    
@end

