import { NativeModules, processColor } from 'react-native';
const NativeTouchID = NativeModules.FingerprintAuth;
// Android provides more flexibility than iOS for handling the Fingerprint. Currently the config object accepts customizable title or color. Otherwise it defaults to this constant

export default {
  isSupported() {
    return new Promise((resolve, reject) => {
      NativeTouchID.isSupported(
        error => {
          return reject(typeof error == 'String' ? createError(error, error) : createError(error));
        },
        success => {
          return resolve(true);
        }
      );
    });
  },

  authenticate(reason, config) {
    var DEFAULT_CONFIG = { title: 'Authentication Required', color: '#1306ff' };
    var authReason = reason ? reason : ' ';
    var authConfig = Object.assign({}, DEFAULT_CONFIG, config);
    var color = processColor(authConfig.color);

    authConfig.color = color;

    return new Promise((resolve, reject) => {
      NativeTouchID.authenticate(
        authReason,
        authConfig,
        error => {
          return reject(typeof error == 'String' ? createError(error, error) : createError(error));
        },
        success => {
          return resolve(true);
        }
      );
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
  return new TouchIDError('Touch ID Error', error);
}
