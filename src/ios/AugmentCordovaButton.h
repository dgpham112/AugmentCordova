//
//  AugmentCordovaButton.h
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AugmentCordovaButton : UIButton

@property (strong, nonatomic) NSString* code;
    
- (void) applyData: (NSDictionary*) data;

@end
