
const ERRORS = require('./data/errors')

function LocalAuthError(name, details) {
  this.name = name || 'LocalAuthError'
  this.message = details.message || 'Local Authentication Error'
  this.details = details || {}
}

LocalAuthError.prototype = Object.create(Error.prototype)
LocalAuthError.prototype.constructor = LocalAuthError

export function createError(error) {
  let details = ERRORS[error]
  details.name = error

  return new LocalAuthError(error, details)
}
