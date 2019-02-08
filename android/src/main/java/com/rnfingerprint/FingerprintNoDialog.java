package com.rnfingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import java.net.SocketOption;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;

public class FingerprintNoDialog implements FingerprintHandler.Callback {

  private FingerprintManager.CryptoObject mCryptoObject;
  private DialogResultHandler dialogCallback;
  private boolean isAuthInProgress;
  private FingerprintHandler mFingerprintHandler;

  private String authReason;

  public void setContext(ReactApplicationContext context){
    this.mFingerprintHandler = new FingerprintHandler(context, this);
  }

  public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
    this.mCryptoObject = cryptoObject;
  }

  public void setCallback(DialogResultHandler newDialogCallback) {
    this.dialogCallback = newDialogCallback;
  }

  public void startAuth() {
    this.isAuthInProgress = true;
    this.mFingerprintHandler.startAuth(mCryptoObject);
  }

  public void setReasonForAuthentication(String reason) {
    this.authReason = reason;
  }

  public interface DialogResultListener {
    void onAuthenticated();

    void onError(String errorString, int errorCode);

    void onCancelled();
  }

  public void onAuthenticated() {
    this.isAuthInProgress = false;
    this.dialogCallback.onAuthenticated();
  }

  public void onError(String errorString, int errorCode) {
      this.dialogCallback.emitErrorMessage(errorString, errorCode);
  }

  public void onCancelled() {
    this.isAuthInProgress = false;
    this.dialogCallback.onCancelled();
    this.mFingerprintHandler.endAuth();
  }
}
