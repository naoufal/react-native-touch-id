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

  authenticate(authReason = ' ') {
    return new Promise((resolve, reject) => {
      NativeTouchID.authenticate(authReason, error => {
        // Return error if rejected
        if (error) {
          return reject(createError(error.message));
        }

        resolve(true);
      });
    });
  }
};

function TouchIDError(name = 'TouchIDError', details = {}) {
  this.name = name;
  this.details = details;
  this.message = details.message || 'Touch ID Error';
}

TouchIDError.prototype = Object.create(Error.prototype);
TouchIDError.prototype.constructor = TouchIDError;

function createError(error) {
  let details = ERRORS[error];
  details.name = error;

  return new TouchIDError(error, details);
}
