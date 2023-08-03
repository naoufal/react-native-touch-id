package com.rnfingerprint;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;

public class DialogResultHandler implements FingerprintDialog.DialogResultListener {
    private Callback errorCallback;
    private Callback successCallback;
    private ReactContext reactContext;

    public DialogResultHandler( Callback reactErrorCallback, Callback reactSuccessCallback, ReactContext context) {
      errorCallback = reactErrorCallback;
      successCallback = reactSuccessCallback;
      reactContext = context;
    }

    @Override
    public void onAuthenticated() {
      FingerprintAuthModule.inProgress = false;
      successCallback.invoke("Successfully authenticated.");
    }

    @Override
    public void onError(String errorString, int errorCode) {
      FingerprintAuthModule.inProgress = false;
        WritableMap params = Arguments.createMap();
        params.putString("errorString", errorString);
        params.putInt("errorCode", errorCode);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onErrorListener", params);
    }
    @Override
    public void onCancelled() {
      FingerprintAuthModule.inProgress = false;
      errorCallback.invoke("cancelled", FingerprintAuthConstants.AUTHENTICATION_CANCELED);
    }
}
