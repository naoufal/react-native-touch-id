package com.rnfingerprint;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.content.Context;
import android.os.CancellationSignal;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private boolean selfCancelled;

    private final FingerprintManager mFingerprintManager;
    private final Callback mCallback;
    private int attemptCount = 1;
    private final int limitAttemt;

    public FingerprintHandler(Context context, Callback callback, int limitAttemt) {
        mFingerprintManager = context.getSystemService(FingerprintManager.class);
        mCallback = callback;
        this.limitAttemt = limitAttemt;
    }

    public void startAuth(FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        selfCancelled = false;
        mFingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void endAuth() {
        cancelAuthenticationSignal();
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        if (!selfCancelled) {
            mCallback.onError(errString.toString());
        }
    }

    @Override
    public void onAuthenticationFailed() {
        if (attemptCount < limitAttemt) {
            attemptCount++;
        } else {
            mCallback.onError("failed");
            cancelAuthenticationSignal();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mCallback.onAuthenticated();
        cancelAuthenticationSignal();
    }

    private void cancelAuthenticationSignal() {
        selfCancelled = true;
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    public interface Callback {
        void onAuthenticated();

        void onError(String errorString);

        void onCancelled();
    }
}
