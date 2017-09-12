package com.rnfingerprint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private boolean selfCancelled;
    private Context appContext;

    private final FingerprintManager mFingerprintManager;
    private final Callback mCallback;

    public FingerprintHandler(Context context, FingerprintManager fingerprintManager, Callback callback) {
        appContext = context;
        mFingerprintManager = fingerprintManager;
        mCallback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        return (android.os.Build.VERSION.SDK_INT >= 23)
                && mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints()
                && (ActivityCompat.checkSelfPermission(appContext,
                        Manifest.permission.USE_FINGERPRINT) ==
                        PackageManager.PERMISSION_GRANTED);
    }

    public void startAuth(FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        selfCancelled = false;

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void endAuth() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
            selfCancelled = true;
        } else {
          mCallback.onError("Authentication Failed");
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        if(!selfCancelled) {
            mCallback.onError(errString.toString());
        } else {
            mCallback.onCancelled();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        mCallback.onError("Authentication Failed");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mCallback.onAuthenticated();
    }

    public interface Callback {
        void onAuthenticated();
        void onError(String errorString);
        void onCancelled();
    }
}
