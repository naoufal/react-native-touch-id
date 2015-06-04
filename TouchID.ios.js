/**
 * @providesModule TouchID
 * @flow
 */
'use strict';

var NativeTouchID = require('NativeModules').TouchID;
var invariant = require('invariant');
var ERRORS = require('./data/errors');

/**
 * High-level docs for the TouchID iOS API can be written here.
 */

var TouchID = {
  authenticate() {
    return new Promise(function(resolve, reject) {
      NativeTouchID.authenticate(function(error, success) {
        // Return error if rejected
        if (error) {
          return reject(createError(error.message));
        }

        resolve(true);
      });
    });
  }
};

function createError(error) {
  var details = ERRORS[error];
  details.name = error;

  return new TouchIDError(error, details);
}

function TouchIDError(name, details) {
  this.name = name || 'TouchIDError';
  this.message = details.message || 'Touch ID Error';
  this.details = details || {};
}

TouchIDError.prototype = Object.create(Error.prototype);
TouchIDError.prototype.constructor = TouchIDError;

module.exports = TouchID;
