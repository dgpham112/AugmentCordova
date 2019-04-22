//
//  AugmentCordovaConfig.h
//
//  Copyright © 2017-present Augment. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * Singleton to keep configuration values that we need to access from everywhere
 */
@interface AugmentCordovaConfig : NSObject 

@property (nonatomic, strong) NSString* AppId;
@property (nonatomic, strong) NSString* AppKey;
@property (nonatomic, strong) NSString* VuforiaKey;

/**
 * This is a NSArray of NSDictionary of NSString, NSString
 *
 * It is a hacky way to allow our external developers to customize the ARView
 * by adding buttons they need that will trigger their Javascript logic
 *
 * see the Javascript documentation for more information
 */
@property (nonatomic, strong) NSMutableArray<NSDictionary*>* UIElements;

+ (id) sharedInstance;

@end
