#import "TouchID.h"
#import "RCTUtils.h"
#import <LocalAuthentication/LocalAuthentication.h>

@implementation TouchID

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(isSupported: (RCTResponseSenderBlock)callback)
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

RCT_EXPORT_METHOD(authenticate: (NSString *)reason
            fallbackToPasscode: (BOOL) fallbackToPasscode
                      callback: (RCTResponseSenderBlock)callback)
{
    [self authenticateWithPolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
                          reason:reason
               completionHandler:^(NSInteger errorCode, NSString* errorReason) {

        if (errorCode == errSecSuccess) {
            callback(@[[NSNull null], @"Authenticated with Touch ID."]);
            return;
        }

        // check if we can fallback
        if (!fallbackToPasscode || (
             errorCode != LAErrorTouchIDNotAvailable &&
             errorCode != LAErrorTouchIDNotEnrolled
            )) {
            callback(@[RCTMakeError(errorReason, nil, nil)]);
            return;
        }

        [self authenticateWithPolicy:LAPolicyDeviceOwnerAuthentication
                              reason:reason
                   completionHandler:^(NSInteger errorCode, NSString *errorReason) {

            if (errorCode == errSecSuccess) {
                callback(@[[NSNull null], @"Authenticated with passcode."]);
                return;
            }

            callback(@[RCTMakeError(errorReason, nil, nil)]);
        }];
    }];
}

- (void) authenticateWithPolicy:(LAPolicy) policy
               reason:(NSString *)reason
    completionHandler:(void(^) (NSInteger, NSString *))handler
{
    LAContext *context = [[LAContext alloc] init];
    __block NSString* errorReason;
    NSError* error;

    if ([context canEvaluatePolicy:policy error:&error]) {
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

    } else {
        // Device does not support TouchID
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
    }
}

@end
