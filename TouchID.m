#import "TouchID.h"
#import <React/RCTUtils.h>

@implementation TouchID

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(isSupported: (RCTResponseSenderBlock)callback)
{
    LAContext *context = [[LAContext alloc] init];
    NSError *error;

    if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
        callback(@[[NSNull null], [self getBiometryType:context]]);
        // Device does not support TouchID
    } else {
        if (error) {
            NSString *errorReason = [self errorReason: error];
            
            NSLog(@"is supported failed: %@", errorReason);
            callback(@[RCTMakeError(errorReason, nil, nil)]);
            return;
        } else {
            callback(@[RCTMakeError(@"RCTTouchIDNotSupported", nil, nil)]);
            return;
        }
    }
}

RCT_EXPORT_METHOD(authenticate: (NSString *)reason
                  callback: (RCTResponseSenderBlock)callback)
{
    LAContext *context = [[LAContext alloc] init];
    NSError *error;

    // Device has TouchID
    if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
        // Attempt Authentification
        [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
                localizedReason:reason
                          reply:^(BOOL success, NSError *error)
         {
             if (success) { // Authentication Successful
                 callback(@[[NSNull null], @"Authenticated with Touch ID."]);
             } else if (error) { // Authentication Error
                 NSString *errorReason = [self errorReason: error];

                 NSLog(@"Authentication failed: %@", errorReason);
                 callback(@[RCTMakeError(errorReason, nil, nil)]);
             } else { // Authentication Failure
                 callback(@[RCTMakeError(@"LAErrorAuthenticationFailed", nil, nil)]);
             }
         }];

        // Device does not support TouchID
    } else {
        callback(@[RCTMakeError(@"RCTTouchIDNotSupported", nil, nil)]);
        return;
    }
}

- (NSString *)getBiometryType:(LAContext *)context
{
    if (@available(iOS 11, *)) {
        return (context.biometryType == LABiometryTypeFaceID) ? @"FaceID" : @"TouchID";
    }

    return @"TouchID";
}

- (NSString *) errorReason:(NSError *) error {
    NSString *errorReason;
    
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
            
        default:
            errorReason = @"RCTTouchIDUnknownError";
            break;
    }
    
    return errorReason;
}

@end

