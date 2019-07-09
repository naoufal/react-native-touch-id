package com.rnfingerprint;

import com.facebook.react.bridge.Callback;

public class KeyguardResultHandler implements KeyguardDialog.KeyguardResultListener  {
    private Callback errorCallback;
    private Callback successCallback;

    public KeyguardResultHandler(Callback reactErrorCallback, Callback reactSuccessCallback) {
        errorCallback = reactErrorCallback;
        successCallback = reactSuccessCallback;
    }

    @Override
    public void onAuthenticated() {
        FingerprintAuthModule.inProgress = false;
        successCallback.invoke("Successfully authenticated.");
    }

    @Override
    public void onCancelled() {
        FingerprintAuthModule.inProgress = false;
        errorCallback.invoke("cancelled", FingerprintAuthConstants.AUTHENTICATION_CANCELED);
    }
}
