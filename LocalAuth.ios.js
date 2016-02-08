/**
 * Mostly a copy of https://github.com/naoufal/react-native-touch-id
 * @providesModule LocalAuth
 * @flow
 */
'use strict'

import { NativeModules } from 'react-native'
let NativeLocalAuth = NativeModules.RNLocalAuth
let ERRORS = require('./data/errors')

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

function LocalAuthError(name, details) {
  this.name = name || 'LocalAuthError'
  this.message = details.message || 'Local Authentication Error'
  this.details = details || {}
}

LocalAuthError.prototype = Object.create(Error.prototype)
LocalAuthError.prototype.constructor = LocalAuthError

function createError(error) {
  let details = ERRORS[error]
  details.name = error

  return new LocalAuthError(error, details)
}

module.exports = LocalAuth
