#import "RNLocalAuth.h"
#import "RCTUtils.h"
#import <LocalAuthentication/LocalAuthentication.h>

@implementation RNLocalAuth

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(hasTouchID: (RCTResponseSenderBlock)callback)
{
    LAContext *context = [[LAContext alloc] init];
    NSError *error;

    if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
        callback(@[[NSNull null], @true]);
        // Device does not support TouchID
    } else {
        callback(@[RCTMakeError(@"RCTTouchIDNotSupported", nil, nil)]);
        return;
    }
}

RCT_EXPORT_METHOD(authenticate: (NSString*)reason
                  fallbackToPasscode:(BOOL)fallbackToPasscode
                  suppressEnterPassword:(BOOL)suppressEnterPassword
                  callback: (RCTResponseSenderBlock)callback)
{
    [self authenticateWithPolicyAsync:LAPolicyDeviceOwnerAuthentication
                               reason:reason
                   fallbackToPasscode:fallbackToPasscode
                suppressEnterPassword:suppressEnterPassword
                          millisDelay:@0
                             callback:callback];
}

RCT_EXPORT_METHOD(authenticateWithTouchID: (NSString*)reason
                  fallbackToPasscode:(BOOL)fallbackToPasscode
                  suppressEnterPassword:(BOOL)suppressEnterPassword
                  callback: (RCTResponseSenderBlock)callback)
{
    [self authenticateWithPolicyAsync:LAPolicyDeviceOwnerAuthenticationWithBiometrics
                               reason:reason
                   fallbackToPasscode:fallbackToPasscode
                suppressEnterPassword:suppressEnterPassword
                          millisDelay:@0
                             callback:callback];
}

- (void) authenticateWithPolicyAsync: (LAPolicy) policy
                              reason:(NSString*)reason
                  fallbackToPasscode:(BOOL)fallbackToPasscode
               suppressEnterPassword:(BOOL)suppressEnterPassword
                         millisDelay:(NSNumber*) millisDelay
                            callback: (RCTResponseSenderBlock)callback
{
    [self authenticateWithPolicy:policy
                          reason:reason
           suppressEnterPassword:suppressEnterPassword
                     millisDelay:millisDelay
               completionHandler:^(NSInteger errorCode, NSString* errorReason) {
                   if (errorCode == errSecSuccess) {
                       callback(@[[NSNull null], @"Authenticated with Touch ID."]);
                       return;
                   }

                   if (errorCode == LAErrorTouchIDLockout) {
                       [self authenticateWithPolicyAsync:LAPolicyDeviceOwnerAuthentication
                                                  reason:reason
                                      fallbackToPasscode:fallbackToPasscode
                                   suppressEnterPassword:suppressEnterPassword
                                             millisDelay:@1000
                                                callback:callback];
                       return;
                   }

                   callback(@[RCTMakeError(errorReason, nil, nil)]);
               }];
}

- (void) authenticateWithPolicy:(LAPolicy) policy
                         reason:(NSString *)reason
          suppressEnterPassword: (BOOL) suppressEnterPassword
                    millisDelay: (NSNumber *) millisDelay
              completionHandler:(void(^) (NSInteger, NSString *))handler
{
    // per http://stackoverflow.com/questions/26463196/touch-id-causing-app-to-become-non-responsive
    if (millisDelay == nil) millisDelay = @0;

    dispatch_queue_t highPriorityQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, [millisDelay intValue] * NSEC_PER_MSEC), highPriorityQueue, ^{
        LAContext *context = [[LAContext alloc] init];
        if (suppressEnterPassword) {
            context.localizedFallbackTitle = @"";
        }

        __block NSString* errorReason;
        NSError* error;

        if (![context canEvaluatePolicy:policy error:&error]) {
            NSInteger errCode;
            NSString* errReason;
            if (policy == LAPolicyDeviceOwnerAuthenticationWithBiometrics) {
                errCode = LAErrorTouchIDNotAvailable;
                errReason = @"RCTTouchIDNotSupported";
            } else {
                errCode = LAErrorAuthenticationFailed;
                errReason = @"LAErrorAuthenticationFailed";
            }

            handler(errCode, errReason);
            return;
        }

        // Attempt Authentification
        [context evaluatePolicy:policy
                localizedReason:reason
                          reply:^(BOOL success, NSError *error)
         {
             // Failed Authentication
             if (error) {
                 switch (error.code) {
                     case LAErrorAuthenticationFailed:
                         errorReason = @"LAErrorAuthenticationFailed";
                         break;

                     case LAErrorUserCancel:
                         errorReason = @"LAErrorUserCancel";
                         break;

                     case LAErrorUserFallback:
                         errorReason = @"LAErrorUserFallback";
                         break;

                     case LAErrorSystemCancel:
                         errorReason = @"LAErrorSystemCancel";
                         break;

                     case LAErrorPasscodeNotSet:
                         errorReason = @"LAErrorPasscodeNotSet";
                         break;

                     case LAErrorTouchIDNotAvailable:
                         errorReason = @"LAErrorTouchIDNotAvailable";
                         break;

                     case LAErrorTouchIDNotEnrolled:
                         errorReason = @"LAErrorTouchIDNotEnrolled";
                         break;

                     case LAErrorTouchIDLockout:
                         errorReason = @"LAErrorTouchIDLockout";
                         break;
                     default:
                         errorReason = @"RCTTouchIDUnknownError";
                         break;
                 }

                 NSLog(@"Authentication failed: %@", errorReason);
                 handler(error.code, errorReason);
                 return;
             }

             handler(errSecSuccess, nil);
             // Authenticated Successfully
         }];
    });
}

@end
