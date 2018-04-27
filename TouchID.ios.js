/**
 * @providesModule TouchID
 * @flow
 */
'use strict';

import { NativeModules } from 'react-native';
const NativeTouchID = NativeModules.TouchID;
const ERRORS = require('./data/errors');

/**
 * High-level docs for the TouchID iOS API can be written here.
 */

export default {
  isSupported() {
    return new Promise((resolve, reject) => {
      NativeTouchID.isSupported((error, biometryType) => {
        if (error) {
          return reject(createError(error.message));
        }

        resolve(biometryType);
      });
    });
  },

  authenticate(reason, config) {
    const DEFAULT_CONFIG = { fallbackLabel: null };
    const authReason = reason ? reason : ' ';
    const authConfig = config ? config : DEFAULT_CONFIG;

    return new Promise((resolve, reject) => {
      NativeTouchID.authenticate(authReason, authConfig, error => {
        // Return error if rejected
        if (error) {
          return reject(createError(error.message));
        }

        resolve(true);
      });
    });
  }
};

function TouchIDError(name, details) {
  this.name = name || 'TouchIDError';
  this.message = details.message || 'Touch ID Error';
  this.details = details || {};
}

TouchIDError.prototype = Object.create(Error.prototype);
TouchIDError.prototype.constructor = TouchIDError;

function createError(error) {
  let details = ERRORS[error];
  details.name = error;

  return new TouchIDError(error, details);
}
