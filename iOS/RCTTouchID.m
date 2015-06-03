//
//  RCTTouchID.m
//  TouchID
//
//  Created by Naoufal Kadhom on 2015-06-02.
//  Copyright (c) 2015 Facebook. All rights reserved.
//

#import "RCTTouchID.h"
#import "RCTUtils.h"
#import <LocalAuthentication/LocalAuthentication.h>

@implementation RCTTouchID

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(authenticate: (RCTResponseSenderBlock)callback)
{
  LAContext *context = [[LAContext alloc] init];
  NSError *error;
  
  // Device has TouchID
  if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
    // Attempt Authentification
    [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
            localizedReason: @" "
                      reply:^(BOOL success, NSError *error)
     {
       // Failed Authentication
       if (error) {
         NSString *errorReason;
         
         switch (error.code) {
           case LAErrorAuthenticationFailed:
             errorReason = @"LAErrorAuthenticationFailed";
             break;
             
           case LAErrorUserCancel:
             errorReason = @"LAErrorUserCancel";
             break;
             
           case LAErrorUserFallback:
             errorReason =  @"LAErrorUserFallback";
             break;
             
           case LAErrorSystemCancel:
             errorReason =  @"LAErrorSystemCancel";
             break;
             
           case LAErrorPasscodeNotSet:
             errorReason =  @"LAErrorPasscodeNotSet";
             break;
             
           case LAErrorTouchIDNotAvailable:
             errorReason =  @"LAErrorTouchIDNotAvailable";
             break;
             
           case LAErrorTouchIDNotEnrolled:
             errorReason =  @"LAErrorTouchIDNotEnrolled";
             break;
             
           default:
             errorReason = @"RCTTouchIDUnknownError";
             break;
         }
         
         NSLog(@"Authentication failed: %@", errorReason);
         callback(@[RCTMakeError(errorReason, nil, nil)]);
         return;
       }
       
       // Authenticated Successfully
       callback(@[[NSNull null], @"Authenticat with Touch ID."]);
     }];
    
    // Device does not support TouchID
  } else {
    callback(@[RCTMakeError(@"RCTTouchIDNotSupported", nil, nil)]);
    return;
  }
}

@end
