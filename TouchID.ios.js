/**
 * @providesModule TouchID
 * @flow
 */
'use strict';

var React = require('react-native');
var {
  NativeModules
} = React;

var NativeTouchID = NativeModules.TouchID;
var ERRORS = require('./data/errors');

/**
 * High-level docs for the TouchID iOS API can be written here.
 */

var TouchID = {
  isSupported() {
    return new Promise(function(resolve, reject) {
      NativeTouchID.isSupported(function(error) {
        if (error) {
          return reject(createError(error.message));
        }

        resolve(true);
      });
    });
  },

  authenticate(reason) {
    var authReason;

    // Set auth reason
    if (reason) {
      authReason = reason;
    // Set as empty string if no reason is passed
    } else {
      authReason = ' ';
    }

    return new Promise(function(resolve, reject) {
      NativeTouchID.authenticate(authReason, function(error) {
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
  var details = ERRORS[error];
  details.name = error;

  return new TouchIDError(error, details);
}

module.exports = TouchID;
