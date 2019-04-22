//
//  AugmentCordovaButton.m
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import "AugmentCordovaButton.h"

static NSString* KEY_CODE             = @"code";
static NSString* KEY_COLOR            = @"color";
static NSString* KEY_TITLE            = @"title";
static NSString* KEY_BORDER_SIZE      = @"borderSize";
static NSString* KEY_BORDER_RADIUS    = @"borderRadius";
static NSString* KEY_BORDER_COLOR     = @"borderColor";
static NSString* KEY_FONT_SIZE        = @"fontSize";
static NSString* KEY_BACKGROUND_COLOR = @"backgroundColor";

@implementation AugmentCordovaButton

- (void) applyData: (NSDictionary*) data {
    if (data[KEY_CODE] != nil) {
        self.code = (NSString*) data[KEY_CODE];
    }
    
    if (data[KEY_TITLE] != nil) {
        [self setTitle: (NSString*) data[KEY_TITLE] forState: UIControlStateNormal];
    }
    
    if (data[KEY_BORDER_SIZE] != nil) {
        self.layer.borderWidth = ((NSString*) data[KEY_BORDER_SIZE]).intValue;
    }

    if (data[KEY_BORDER_RADIUS] != nil) {
        self.layer.cornerRadius = ((NSString*) data[KEY_BORDER_RADIUS]).intValue;
    }
    
    if (data[KEY_BORDER_COLOR] != nil) {
        self.layer.borderColor = [AugmentCordovaButton ColorFromHexString: (NSString*) data[KEY_BORDER_COLOR]].CGColor;
    }
    
    if (data[KEY_FONT_SIZE] != nil) {
        self.titleLabel.font = [UIFont systemFontOfSize: ((NSString*) data[KEY_FONT_SIZE]).intValue];
    }
    
    if (data[KEY_COLOR] != nil) {
        UIColor* color = [AugmentCordovaButton ColorFromHexString: (NSString*) data[KEY_COLOR]];
        [self setTitleColor: color forState: UIControlStateNormal];
        self.tintColor = color;
    }
    
    if (data[KEY_BACKGROUND_COLOR] != nil) {
        self.backgroundColor = [AugmentCordovaButton ColorFromHexString: (NSString*) data[KEY_BACKGROUND_COLOR]];
    }
}
    
#pragma mark - Helpers

+ (UIColor*) ColorFromHexString: (NSString*) hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString: hexString];
    [scanner setScanLocation: 1]; // bypass '#' character
    [scanner scanHexInt: &rgbValue];
    return [UIColor colorWithRed: ((rgbValue & 0xFF0000) >> 16)/255.0 green: ((rgbValue & 0xFF00) >> 8)/255.0 blue: (rgbValue & 0xFF)/255.0 alpha: 1.0];
}

@end
