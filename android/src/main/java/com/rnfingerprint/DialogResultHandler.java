package com.rnfingerprint;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class DialogResultHandler implements FingerprintDialog.DialogResultListener {
    private Callback errorCallback;
    private Callback successCallback;
    private ReactApplicationContext context;

    public DialogResultHandler(Callback reactErrorCallback, Callback reactSuccessCallback, ReactApplicationContext reactContext) {
      errorCallback = reactErrorCallback;
      successCallback = reactSuccessCallback;
      context = reactContext;
    }

    @Override
    public void onAuthenticated() {
      FingerprintAuthModule.inProgress = false;
      successCallback.invoke("Successfully authenticated.");
    }

    @Override
    public void onError(String errorString, int errorCode) {
      FingerprintAuthModule.inProgress = false;
      errorCallback.invoke(errorString, errorCode);
    }

    public void emitErrorMessage(String message, int code) {
      FingerprintAuthModule.inProgress = false;

      WritableMap params = Arguments.createMap();
      params.putString("message", message);
      params.putInt("code", code);
      context
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("authError", params);

    }

    @Override
    public void onCancelled() {
      FingerprintAuthModule.inProgress = false;
      errorCallback.invoke("cancelled", FingerprintAuthConstants.AUTHENTICATION_CANCELED);
    }
}
