module.exports = {
  LAErrorAuthenticationFailed: {
    message: 'Authentication was not successful because the user failed to provide valid credentials.'
  },
  LAErrorUserCancel: {
    message: 'Authentication was canceled by the user—for example, the user tapped Cancel in the dialog.'
  },
  LAErrorUserFallback: {
    message: 'Authentication was canceled because the user tapped the fallback button (Enter Password).'
  },
  LAErrorSystemCancel: {
    message: 'Authentication was canceled by system—for example, if another application came to foreground while the authentication dialog was up.'
  },
  LAErrorPasscodeNotSet: {
    message: 'Authentication could not start because the passcode is not set on the device.'
  },
  LAErrorBiometryNotAvailable: {
    message: 'Authentication could not start because Biometry is not available on the device.'
  },
  LAErrorBiometryNotEnrolled: {
    message: 'Authentication could not start because Biometry has no enrolled biometric identities.'
  },
  LAErrorBiometryLockout: {
    message: 'Authentication could not start because Biometry had too many failed attempts.'
  },
  LAErrorTouchIDNotAvailable: {
    message: 'Authentication could not start because Touch ID is not available on the device.'
  },
  LAErrorTouchIDNotEnrolled: {
    message: 'Authentication could not start because Touch ID has no enrolled fingers.'
  },
  LAErrorTouchIDLockout: {
    message: 'Authentication could not start because Touch ID had too many failed attempts.'
  },
  RCTTouchIDUnknownError: {
    message: 'Could not authenticate for an unknown reason.'
  },
  RCTTouchIDNotSupported: {
    message: 'Device does not support Touch ID.'
  }
};
