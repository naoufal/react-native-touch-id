/**
 * Mostly a copy of https://github.com/naoufal/react-native-touch-id
 * @providesModule LocalAuth
 * @flow
 */
'use strict'

import { createError } from './error'
import Errors from './data/errors'

const noTouchID = new Promise.reject(createError(Errors.RCTTouchIDNotSupported))

module.exports = {
  hasTouchID() {
    return noTouchID
  },

  authenticate(opts) {
    return noTouchID
  }
}
