package com.rnfingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.concurrent.Executor;

import javax.crypto.Cipher;

public class FingerprintAuthModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String FRAGMENT_TAG = "fingerprint_dialog";

    private KeyguardManager keyguardManager;
    private boolean isAppActive;

    public static boolean inProgress = false;

    public FingerprintAuthModule(final ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addLifecycleEventListener(this);
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

        int result = isFingerprintAuthAvailable();
        if (result == FingerprintAuthConstants.IS_SUPPORTED) {
            reactSuccessCallback.invoke("Fingerprint");
        } else {
            reactErrorCallback.invoke("Not supported.", result);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @ReactMethod
    public void authenticate(final String reason, final ReadableMap authConfig, final Callback reactErrorCallback, final Callback reactSuccessCallback) {
        final FragmentActivity activity = (FragmentActivity) getCurrentActivity();
        if (inProgress || !isAppActive || activity == null) {
            return;
        }
        inProgress = true;

        int availableResult = isFingerprintAuthAvailable();
        if (availableResult != FingerprintAuthConstants.IS_SUPPORTED) {
            inProgress = false;
            reactErrorCallback.invoke("Not supported", availableResult);
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                switch (errorCode) {
                    case BiometricConstants.ERROR_USER_CANCELED:
                    case BiometricConstants.ERROR_NEGATIVE_BUTTON:
                        reactErrorCallback.invoke(errString, FingerprintAuthConstants.AUTHENTICATION_FAILED);
                        break;
                    case BiometricConstants.ERROR_CANCELED:
                    case BiometricConstants.ERROR_HW_NOT_PRESENT:
                    case BiometricConstants.ERROR_HW_UNAVAILABLE:
                    case BiometricConstants.ERROR_LOCKOUT:
                    case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                    case BiometricConstants.ERROR_NO_BIOMETRICS:
                    case BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL:
                    case BiometricConstants.ERROR_NO_SPACE:
                    case BiometricConstants.ERROR_TIMEOUT:
                    case BiometricConstants.ERROR_UNABLE_TO_PROCESS:
                    case BiometricConstants.ERROR_VENDOR:
                        break;
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                reactSuccessCallback.invoke("Successfully authenticated.");
            }
        };

        final BiometricPrompt prompt = new BiometricPrompt(activity, executor, callback);

        final Cipher cipher = new FingerprintCipher().getCipher();
        if (cipher == null) {
            inProgress = false;
            reactErrorCallback.invoke("Not supported", FingerprintAuthConstants.NOT_AVAILABLE);
            return;
        }

//        final BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);

        String cancelText = "Logout";

        if (authConfig.hasKey("cancelText")) {
            cancelText = authConfig.getString("cancelText");
        }

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(reason)
                .setConfirmationRequired(true)
//                .setNegativeButtonText(cancelText)
                .setDeviceCredentialAllowed(true)
                .build();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new BiometricBackground().show(activity.getSupportFragmentManager(), "bg");
                prompt.authenticate(promptInfo);
            }
        });

        if (!isAppActive) {
            inProgress = false;
            return;
        }

    }

    private int isFingerprintAuthAvailable() {
        if (android.os.Build.VERSION.SDK_INT < 23) {
            return FingerprintAuthConstants.NOT_SUPPORTED;
        }

        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return FingerprintAuthConstants.NOT_AVAILABLE; // we can't do the check
        }

        BiometricManager biometricManager = BiometricManager.from(activity);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return FingerprintAuthConstants.NOT_AVAILABLE;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return FingerprintAuthConstants.NOT_ENROLLED;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return FingerprintAuthConstants.NOT_PRESENT;
            case BiometricManager.BIOMETRIC_SUCCESS:
                return FingerprintAuthConstants.IS_SUPPORTED;
        }
        return FingerprintAuthConstants.AUTHENTICATION_FAILED;
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
