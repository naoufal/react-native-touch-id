/**
 * @providesModule TouchID
 * @flow
 */
'use strict';

import { NativeModules } from 'react-native';
const NativeTouchID = NativeModules.TouchID;
const { iOSErrors } = require('./data/errors');
const { getError, TouchIDError, TouchIDUnifiedError } = require('./errors');

/**
 * High-level docs for the TouchID iOS API can be written here.
 */

export default {
  isSupported(config) {
    return new Promise((resolve, reject) => {
      NativeTouchID.isSupported(config, (error, biometryType) => {
        if (error) {
          return reject(createError(config, error.message));
        }

        resolve(biometryType);
      });
    });
  },

  authenticate(reason, config) {
    const DEFAULT_CONFIG = {
      fallbackLabel: null,
      unifiedErrors: false,
      passcodeFallback: false
    };
    const authReason = reason ? reason : ' ';
    const authConfig = Object.assign({}, DEFAULT_CONFIG, config);

    return new Promise((resolve, reject) => {
      NativeTouchID.authenticate(authReason, authConfig, error => {
        // Return error if rejected
        if (error) {
          return reject(createError(authConfig, error.message));
        }

        resolve(true);
      });
    });
  }
};

function createError(config, error) {
  const { unifiedErrors } = config || {};

  if (unifiedErrors) {
    return new TouchIDUnifiedError(getError(error));
  }

  const details = iOSErrors[error];
  details.name = error;

  return new TouchIDError(error, details);
}
