package com.rnfingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import javax.crypto.Cipher;

public class FingerprintAuthModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String FRAGMENT_TAG = "fingerprint_dialog";

    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private boolean isAppActive;

    public static boolean inProgress = false;

    public FingerprintAuthModule(final ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addLifecycleEventListener(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private FingerprintManager.CryptoObject getCryptoObject() {
        if (cryptoObject != null) {
            return cryptoObject;
        }

        final Cipher cipher = new FingerprintCipher().getCipher();
        cryptoObject = new FingerprintManager.CryptoObject(cipher);

        return cryptoObject;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private FingerprintManager getFingerprintManager() {
        if (fingerprintManager != null) {
            return fingerprintManager;
        }

        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return null;
        }
        fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);

        return fingerprintManager;
    }

    private KeyguardManager getKeyguardManager() {
        if (keyguardManager != null) {
            return keyguardManager;
        }
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return null;
        }

        keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);

        return keyguardManager;
    }

    @Override
    public String getName() {
        return "FingerprintAuth";
    }

    @ReactMethod
    public void isSupported(final Callback reactErrorCallback, final Callback reactSuccessCallback) {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        if (!isFingerprintAuthAvailable()) {
            reactErrorCallback.invoke("Not supported.");
        } else {
            reactSuccessCallback.invoke("Is supported.");
        }
    }

    @ReactMethod
    public void authenticate(final String reason, final ReadableMap authConfig, final Callback reactErrorCallback, final Callback reactSuccessCallback) {
        final Activity activity = getCurrentActivity();
        if (inProgress || !isAppActive || activity == null) {
            return;
        }
        inProgress = true;

        if (!isFingerprintAuthAvailable()) {
            inProgress = false;
            reactErrorCallback.invoke("Not supported");
            return;
        }

        final FingerprintManager.CryptoObject cryptoObject = this.getCryptoObject();
        if (cryptoObject == null) {
            inProgress = false;
            reactErrorCallback.invoke("Not supported");
            return;
        }

        /* FINGERPRINT ACTIVITY RELATED STUFF */
        final DialogResultHandler drh = new DialogResultHandler(reactErrorCallback, reactSuccessCallback);
        final FingerprintDialog fingerprintDialog = new FingerprintDialog();
        fingerprintDialog.setCryptoObject(cryptoObject);
        fingerprintDialog.setReasonForAuthentication(reason);
        fingerprintDialog.setAuthConfig(authConfig);
        fingerprintDialog.setDialogCallback(drh);

        if (!isAppActive) {
            inProgress = false;
            return;
        }

        fingerprintDialog.show(activity.getFragmentManager(), FRAGMENT_TAG);
    }

    private boolean isFingerprintAuthAvailable() {
        if (android.os.Build.VERSION.SDK_INT < 23) {
            return false;
        }

        final KeyguardManager keyguardManager = getKeyguardManager();
        final FingerprintManager fingerprintManager = getFingerprintManager();

        if (keyguardManager == null || !keyguardManager.isKeyguardSecure()) {
            return false;
        }

        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            return false;
        }

        return fingerprintManager.hasEnrolledFingerprints();
    }

    @Override
    public void onHostResume() {
        isAppActive = true;
    }

    @Override
    public void onHostPause() {
        isAppActive = false;
    }

    @Override
    public void onHostDestroy() {
        isAppActive = false;
    }
}
