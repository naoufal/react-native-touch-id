/**
 * @providesModule TouchID
 * @flow
 */
'use strict';

var NativeTouchID = require('NativeModules').TouchID;
var invariant = require('invariant');

/**
 * High-level docs for the TouchID iOS API can be written here.
 */

var TouchID = {
  authenticate: NativeTouchID.authenticate
};

module.exports = TouchID;
