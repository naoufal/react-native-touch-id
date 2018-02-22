package com.rnfingerprint;

import com.facebook.react.bridge.Callback;

import android.util.Log;

public class DialogResultHandler implements FingerprintDialog.DialogResultListener {
    private Callback errorCallback;
    private Callback successCallback;

    public DialogResultHandler(Callback reactErrorCallback, Callback reactSuccessCallback) {
      errorCallback = reactErrorCallback;
      successCallback = reactSuccessCallback;
    };

    @Override
    public void onAuthenticated() {
      FingerprintAuthModule.inProgress = false;
      if (successCallback != null) {
        successCallback.invoke("Successfully authenticated.");
        errorCallback = successCallback = null;
      }
    }
    @Override
    public void onError(String errorString) {
      FingerprintAuthModule.inProgress = false;
      if (errorCallback != null) {
        errorCallback.invoke(errorString);
        errorCallback = successCallback = null;
      }
    }
    @Override
    public void onCancelled() {
      FingerprintAuthModule.inProgress = false;
      if (errorCallback != null) {
        errorCallback.invoke("cancelled");
        errorCallback = successCallback = null;
      }
    }
}
