package com.rnfingerprint;

import com.facebook.react.bridge.Callback;

public class DialogResultHandler implements FingerprintDialog.DialogResultListener {
    private Callback errorCallback;
    private Callback successCallback;

    public DialogResultHandler(Callback reactErrorCallback, Callback reactSuccessCallback) {
      errorCallback = reactErrorCallback;
      successCallback = reactSuccessCallback;
    }

    @Override
    public void onAuthenticated() {
      FingerprintAuthModule.inProgress = false;
      successCallback.invoke("Successfully authenticated.");
    }
    @Override
    public void onError(String errorString) {
      FingerprintAuthModule.inProgress = false;
      errorCallback.invoke(errorString);
    }
    @Override
    public void onCancelled() {
      FingerprintAuthModule.inProgress = false;
      errorCallback.invoke("cancelled");
    }
}
