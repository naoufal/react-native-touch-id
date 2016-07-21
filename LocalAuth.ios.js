/**
 * Mostly a copy of https://github.com/naoufal/react-native-touch-id
 * @providesModule LocalAuth
 * @flow
 */
'use strict'

import { NativeModules } from 'react-native'
import { createError } from './error'

let NativeLocalAuth = NativeModules.RNLocalAuth
let LocalAuth = {
  hasTouchID() {
    return new Promise(function(resolve, reject) {
      NativeLocalAuth.hasTouchID(function(error) {
        if (error) {
          return reject(createError(error.message))
        }

        resolve()
      })
    })
  },

  authenticate(opts) {
    return new Promise(function(resolve, reject) {
      NativeLocalAuth.authenticate(
        opts.reason || '',
        !!opts.fallbackToPasscode,
        !!opts.suppressEnterPassword,
        function (error) {
          if (error) reject(createError(error.message))
          else resolve()
        }
      )
    })
  }
}

module.exports = LocalAuth
