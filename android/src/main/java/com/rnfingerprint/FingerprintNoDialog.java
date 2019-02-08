package com.rnfingerprint;

import android.hardware.fingerprint.FingerprintManager;
import com.facebook.react.bridge.ReactApplicationContext;

public class FingerprintNoDialog implements FingerprintHandler.Callback {

  private FingerprintManager.CryptoObject mCryptoObject;
  private DialogResultHandler dialogCallback;
  private FingerprintHandler mFingerprintHandler;

  void setContext(ReactApplicationContext context){
    this.mFingerprintHandler = new FingerprintHandler(context, this);
  }

  void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
    this.mCryptoObject = cryptoObject;
  }

  void setCallback(DialogResultHandler newDialogCallback) {
    this.dialogCallback = newDialogCallback;
  }

  void startAuth() {
    this.mFingerprintHandler.startAuth(mCryptoObject);
  }

  public void onAuthenticated() {
    this.dialogCallback.onAuthenticated();
  }

  public void onError(String errorString, int errorCode) {
      this.dialogCallback.emitErrorMessage(errorString, errorCode);
  }

  public void onCancelled() {
    this.dialogCallback.onCancelled();
    this.mFingerprintHandler.endAuth();
  }
}
