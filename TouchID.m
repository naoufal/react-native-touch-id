#import "TouchID.h"
#import "RCTUtils.h"
#import <LocalAuthentication/LocalAuthentication.h>

@implementation TouchID

NSString *const keychainItemIdentifier = @"react-native-touch-id-passcode-trigger";
NSString *const keychainItemServiceName = @"react-native-touch-id";
BOOL storedDummy = false;

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
    [self authenticateWithTouchID:reason completionHandler:^(NSInteger errorCode, NSString* errorReason) {
        if (errorCode == errSecSuccess) {
            callback(@[[NSNull null], @"Authenticated with Touch ID."]);
            return;
        }

        // check if we can fallback
        if (fallbackToPasscode &&
            (errorCode == LAErrorTouchIDNotAvailable || errorCode == LAErrorTouchIDNotEnrolled)) {
            [self fallbackToPasscode:reason errorReason:errorReason callback:callback];
            return;
        }

        callback(@[RCTMakeError(errorReason, nil, nil)]);
    }];
}

- (void) authenticateWithTouchID:(NSString *)reason
                    completionHandler:(void(^) (NSInteger, NSString *))handler
{
    LAContext *context = [[LAContext alloc] init];
    __block NSString* errorReason;
    NSError* error;

    // Device has TouchID
    if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
        // Attempt Authentification
        [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
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

        // Device does not support TouchID
    } else {
        handler(LAErrorTouchIDNotAvailable, @"RCTTouchIDNotSupported");
    }
}

- (void) fallbackToPasscode:(NSString*) reason
                errorReason:(NSString*) errorReason
                  callback: (RCTResponseSenderBlock) callback
{
    // technique + code from: https://www.secsign.com/fingerprint-validation-as-an-alternative-to-passcodes/
    if (![self storeDummyKeychainItem]) {
        callback(@[RCTMakeError(errorReason, nil, nil)]);
        return;
    }

    // The keychain operation shall be performed by the global queue. Otherwise it might just not happen.
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void) {

        // Create the keychain query attributes using the values from the first part of the code.
        NSMutableDictionary * query = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                       (__bridge id)(kSecClassGenericPassword), kSecClass,
                                       keychainItemIdentifier, kSecAttrAccount,
                                       keychainItemServiceName, kSecAttrService,
                                       reason, kSecUseOperationPrompt,
                                       nil];

        // Start the query and the fingerprint scan and/or device passcode validation
        CFTypeRef result = nil;
        OSStatus authStatus = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);

        // Ignore the found content of the key chain entry (the dummy password) and only evaluate the return code.
        if (authStatus == errSecSuccess) {
            callback(@[[NSNull null], @"Executed fallback to passcode."]);
        } else {
            callback(@[RCTMakeError(errorReason, nil, nil)]);
        }
    });
}

- (BOOL) storeDummyKeychainItem
{
    if (storedDummy) return true;

    // technique + code from: https://www.secsign.com/fingerprint-validation-as-an-alternative-to-passcodes/
    NSData * pwData = [@"the password itself does not matter" dataUsingEncoding:NSUTF8StringEncoding];
    NSMutableDictionary	* attributes = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                        (__bridge id)(kSecClassGenericPassword), kSecClass,
                                        keychainItemIdentifier, kSecAttrAccount,
                                        keychainItemServiceName, kSecAttrService, nil];

    // Require a fingerprint scan or passcode validation when the keychain entry is read.
    CFErrorRef accessControlError = NULL;
    SecAccessControlRef accessControlRef = SecAccessControlCreateWithFlags(
                                                                           kCFAllocatorDefault,
                                                                           kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
                                                                           kSecAccessControlUserPresence,
                                                                           &accessControlError);

    if (accessControlRef == NULL || accessControlError != NULL)
    {
        NSLog(@"Cannot create SecAccessControlRef to store a password with identifier “%@” in the key chain: %@.", keychainItemIdentifier, accessControlError);
        return false;
    }

    attributes[(__bridge id)kSecAttrAccessControl] = (__bridge id)accessControlRef;

    // In case this code is executed again and the keychain item already exists we want an error code instead of a fingerprint scan.
    attributes[(__bridge id)kSecUseNoAuthenticationUI] = @YES;
    attributes[(__bridge id)kSecValueData] = pwData;

    CFTypeRef result;
    OSStatus osStatus = SecItemAdd((__bridge CFDictionaryRef)attributes, &result);
    storedDummy = osStatus == errSecSuccess || osStatus == errSecDuplicateItem;
    return storedDummy;
}

@end
