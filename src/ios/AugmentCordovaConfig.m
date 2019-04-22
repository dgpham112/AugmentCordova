//
//  AugmentCordovaConfig.m
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import "AugmentCordovaConfig.h"

@implementation AugmentCordovaConfig

+ (id) sharedInstance {
    static AugmentCordovaConfig *shared = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        shared = [self new];
    });
    return shared;
}

- (id) init {
    if (self = [super init]) {
        self.UIElements = [NSMutableArray new];
    }
    return self;
}

@end
