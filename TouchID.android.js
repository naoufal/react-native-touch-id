/**
 * Stub of TouchID for Android.
 *
 * @providesModule TouchID
 * @flow
 */
'use strict';

export default {
  isSupported() {
    return Promise.reject(new Error('Android does not support TouchID.'));
  }
};
