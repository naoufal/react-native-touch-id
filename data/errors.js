const codes = {
  iOSCodes: {
    LAErrorAuthenticationFailed: 'LAErrorAuthenticationFailed',
    LAErrorUserCancel: 'LAErrorUserCancel',
    LAErrorUserFallback: 'LAErrorUserFallback',
    LAErrorSystemCancel: 'LAErrorSystemCancel',
    LAErrorPasscodeNotSet: 'LAErrorPasscodeNotSet',
    LAErrorTouchIDNotAvailable: 'LAErrorTouchIDNotAvailable',
    LAErrorTouchIDNotEnrolled: 'LAErrorTouchIDNotEnrolled',
    RCTTouchIDNotSupported: 'RCTTouchIDNotSupported',
    RCTTouchIDUnknownError: 'RCTTouchIDUnknownError'
  },
  androidApiCodes: {
    FINGERPRINT_ERROR_HW_UNAVAILABLE: 'FINGERPRINT_ERROR_HW_UNAVAILABLE',
    FINGERPRINT_ERROR_UNABLE_TO_PROCESS: 'FINGERPRINT_ERROR_UNABLE_TO_PROCESS',
    FINGERPRINT_ERROR_TIMEOUT: 'FINGERPRINT_ERROR_TIMEOUT',
    FINGERPRINT_ERROR_NO_SPACE: 'FINGERPRINT_ERROR_NO_SPACE',
    FINGERPRINT_ERROR_CANCELED: 'FINGERPRINT_ERROR_CANCELED',
    FINGERPRINT_ERROR_LOCKOUT: 'FINGERPRINT_ERROR_LOCKOUT',
    FINGERPRINT_ERROR_VENDOR: 'FINGERPRINT_ERROR_VENDOR',
    FINGERPRINT_ERROR_LOCKOUT_PERMANENT: 'FINGERPRINT_ERROR_LOCKOUT_PERMANENT',
    FINGERPRINT_ERROR_USER_CANCELED: 'FINGERPRINT_ERROR_USER_CANCELED',
    FINGERPRINT_ERROR_NO_FINGERPRINTS: 'FINGERPRINT_ERROR_NO_FINGERPRINTS',
    FINGERPRINT_ERROR_HW_NOT_PRESENT: 'FINGERPRINT_ERROR_HW_NOT_PRESENT'
  },
  androidModuleCodes: {
    NOT_SUPPORTED: 'NOT_SUPPORTED',
    NOT_PRESENT: 'NOT_PRESENT',
    NOT_AVAILABLE: 'NOT_AVAILABLE',
    NOT_ENROLLED: 'NOT_ENROLLED',
    AUTHENTICATION_FAILED: 'AUTHENTICATION_FAILED',
    AUTHENTICATION_CANCELED: 'AUTHENTICATION_CANCELED'
  }
};

const iOSErrors = {
  [codes.iOSCodes.LAErrorAuthenticationFailed]: {
    message: 'Authentication was not successful because the user failed to provide valid credentials.'
  },
  [codes.iOSCodes.LAErrorUserCancel]: {
    message: 'Authentication was canceled by the user—for example, the user tapped Cancel in the dialog.'
  },
  [codes.iOSCodes.LAErrorUserFallback]: {
    message: 'Authentication was canceled because the user tapped the fallback button (Enter Password).'
  },
  [codes.iOSCodes.LAErrorSystemCancel]: {
    message: 'Authentication was canceled by system—for example, if another application came to foreground while the authentication dialog was up.'
  },
  [codes.iOSCodes.LAErrorPasscodeNotSet]: {
    message: 'Authentication could not start because the passcode is not set on the device.'
  },
  [codes.iOSCodes.LAErrorTouchIDNotAvailable]: {
    message: 'Authentication could not start because Touch ID is not available on the device'
  },
  [codes.iOSCodes.LAErrorTouchIDNotEnrolled]: {
    message: 'Authentication could not start because Touch ID has no enrolled fingers.'
  },
  [codes.iOSCodes.RCTTouchIDUnknownError]: {
    message: 'Could not authenticate for an unknown reason.'
  },
  [codes.iOSCodes.RCTTouchIDNotSupported]: {
    message: 'Device does not support Touch ID.'
  }
};

const androidApiErrorMap = {
  1: codes.androidApiCodes.FINGERPRINT_ERROR_HW_UNAVAILABLE,
  2: codes.androidApiCodes.FINGERPRINT_ERROR_UNABLE_TO_PROCESS,
  3: codes.androidApiCodes.FINGERPRINT_ERROR_TIMEOUT,
  5: codes.androidApiCodes.FINGERPRINT_ERROR_CANCELED,
  7: codes.androidApiCodes.FINGERPRINT_ERROR_LOCKOUT,
  8: codes.androidApiCodes.FINGERPRINT_ERROR_VENDOR,
  9: codes.androidApiCodes.FINGERPRINT_ERROR_LOCKOUT_PERMANENT,
  10: codes.androidApiCodes.FINGERPRINT_ERROR_USER_CANCELED,
  11: codes.androidApiCodes.FINGERPRINT_ERROR_NO_FINGERPRINTS,
  12: codes.androidApiCodes.FINGERPRINT_ERROR_HW_NOT_PRESENT
};

const androidModuleErrorMap = {
  101: codes.androidModuleCodes.NOT_SUPPORTED,
  102: codes.androidModuleCodes.NOT_PRESENT,
  103: codes.androidModuleCodes.NOT_AVAILABLE,
  104: codes.androidModuleCodes.NOT_ENROLLED,
  105: codes.androidModuleCodes.AUTHENTICATION_FAILED,
  106: codes.androidModuleCodes.AUTHENTICATION_CANCELED
};

const errors = {
  AUTHENTICATION_FAILED: {
    message: 'Authentication failed',
    code: 'AUTHENTICATION_FAILED'
  },
  USER_CANCELED: {
    message: 'User canceled authentication',
    code: 'USER_CANCELED'
  },
  SYSTEM_CANCELED: {
    message: 'System canceled authentication',
    code: 'SYSTEM_CANCELED'
  },
  NOT_PRESENT: {
    message: 'Biometry hardware not present',
    code: 'NOT_PRESENT'
  },
  NOT_SUPPORTED: {
    message: 'Biometry is not supported',
    code: 'NOT_SUPPORTED'
  },
  NOT_AVAILABLE: {
    message: 'Biometry is not currently available',
    code: 'NOT_AVAILABLE'
  },
  NOT_ENROLLED: {
    message: 'Biometry is not enrolled',
    code: 'NOT_ENROLLED'
  },
  TIMEOUT: {
    message: 'Biometry timeout',
    code: 'TIMEOUT'
  },
  LOCKOUT: {
    message: 'Biometry lockout',
    code: 'LOCKOUT'
  },
  LOCKOUT_PERMANENT: {
    message: 'Biometry permanent lockout',
    code: 'LOCKOUT_PERMANENT'
  },
  PROCESSING_ERROR: {
    message: 'Biometry processing error',
    code: 'PROCESSING_ERROR'
  },
  USER_FALLBACK: {
    message: 'User selected fallback',
    code: 'USER_FALLBACK'
  },
  FALLBACK_NOT_ENROLLED: {
    message: 'User selected fallback not enrolled',
    code: 'FALLBACK_NOT_ENROLLED'
  },
  UNKNOWN_ERROR: {
    message: 'Unknown error',
    code: 'UNKNOWN_ERROR'
  }
};

module.exports = {
  codes,
  iOSErrors,
  androidApiErrorMap,
  androidModuleErrorMap,
  errors
};
