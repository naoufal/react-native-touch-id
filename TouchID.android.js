import { NativeModules, processColor } from 'react-native';
import { androidApiErrorMap, androidModuleErrorMap } from './data/errors';
import { getError, TouchIDError, TouchIDUnifiedError } from './errors';
const NativeTouchID = NativeModules.FingerprintAuth;

export default {
  isSupported(config) {
    return new Promise((resolve, reject) => {
      NativeTouchID.isSupported(
        (error, code) => {
          return reject(createError(config, error, code));
        },
        success => {
          return resolve(true);
        }
      );
    });
  },

  authenticate(reason, config) {
    DEFAULT_CONFIG = {
      title: 'Authentication Required',
      color: '#1306ff',
      sensorDescription: 'Touch sensor',
      cancelText: 'Cancel',
      unifiedErrors: false
    };
    var authReason = reason ? reason : ' ';
    var authConfig = Object.assign({}, DEFAULT_CONFIG, config);
    var color = processColor(authConfig.color);

    authConfig.color = color;

    return new Promise((resolve, reject) => {
      NativeTouchID.authenticate(
        authReason,
        authConfig,
        (error, code) => {
          return reject(createError(authConfig, error, code));
        },
        success => {
          return resolve(true);
        }
      );
    });
  }
};

function createError(config, error, code) {
  const { unifiedErrors } = config || {};
  const errorCode = androidApiErrorMap[code] || androidModuleErrorMap[code];

  if (unifiedErrors) {
    return new TouchIDUnifiedError(getError(errorCode));
  }

  return new TouchIDError('Touch ID Error', error, errorCode);
}
