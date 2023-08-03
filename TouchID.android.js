import { NativeModules, processColor, NativeEventEmitter } from 'react-native';
import { androidApiErrorMap, androidModuleErrorMap } from './data/errors';
import { getError, TouchIDError, TouchIDUnifiedError } from './errors';
const NativeTouchID = NativeModules.FingerprintAuth;

let listener;

export default {
  isSupported(config) {
    return new Promise((resolve, reject) => {
      NativeTouchID.isSupported(
        (error, code) => {
          return reject(createError(config, error, code));
        },
        (biometryType) => {
          return resolve(biometryType);
        }
      );
    });
  },

  authenticate(reason, config) {
    var DEFAULT_CONFIG = {
      title: '',
      imageColor: '',
      imageErrorColor: '',
      sensorDescription: '',
      sensorErrorDescription: '',
      cancelText: '',
      unifiedErrors: false
    };
    var authReason = reason ? reason : ' ';
    var authConfig = Object.assign({}, DEFAULT_CONFIG, config);
    var imageColor = processColor(authConfig.imageColor);
    var imageErrorColor = processColor(authConfig.imageErrorColor);

    authConfig.imageColor = imageColor;
    authConfig.imageErrorColor = imageErrorColor;

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
  },
  addListener(callback) {
    const event = new NativeEventEmitter(NativeTouchID);
    listener = event.addListener('onErrorListener', (ev) => {
      callback(createError({}, ev.errorString, ev.errorCode));
    });
  },
  remove() {
    listener.remove();
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
