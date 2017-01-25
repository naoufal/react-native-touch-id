/**
 * Mostly a copy of https://github.com/naoufal/react-native-touch-id
 * @providesModule LocalAuth
 * @flow
 */
'use strict'

import { NativeModules } from 'react-native'
import { createError } from './error'
import PasscodeStatus from 'react-native-passcode-status'

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

  isDeviceSecure() {
    if (!PasscodeStatus.supported) {
      return Promise.reject(new Error('unable to determine'))
    }

    return new Promise((resolve, reject) => {
      PasscodeStatus.get(function (err, status) {
        if (err) return reject(new Error(err))
        if (status === 'unknown') return reject(new Error('unable to determine'))

        resolve(status === 'enabled')
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
