package com.rnfingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.util.concurrent.Executor;

import static android.os.Looper.getMainLooper;

@ReactModule(name = FingerprintAuthModule.NAME)
public class FingerprintAuthModule extends ReactContextBaseJavaModule implements LifecycleEventListener, RetryCallback {
    public static final String NAME = "FingerprintAuth";
    private static final String FRAGMENT_TAG = "fingerprint_dialog";

    private KeyguardManager keyguardManager;

    private boolean isAppActive = false;
    private boolean authSuccess = false;
    private boolean inProgress = false;
    private Callback reactSuccessCallback;

    private BiometricBackground background;
    private BiometricPrompt prompt;
    private BiometricPrompt.PromptInfo promptInfo;


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
        authSuccess = false;
        this.reactSuccessCallback = reactSuccessCallback;

        String cancelText = "Cancel";
        if (authConfig.hasKey("cancelText")) {
            cancelText = authConfig.getString("cancelText");
        }

        String retryText = "Retry";
        if (authConfig.hasKey("retryText")) {
            retryText = authConfig.getString("retryText");
        }

        if (authConfig.getBoolean("useBackground")) {
            background = new BiometricBackground();
            background.setLogoUrl("https://www.managebac.com/wp-content/uploads/2020/07/ManageBac-vertical@2x-1024x758-1.png");
            background.setCancelButtonText(cancelText);
            background.setCancelListener(new Callback() {
                @Override
                public void invoke(Object... args) {
                    inProgress = false;
                    reactErrorCallback.invoke("User cancelled", BiometricConstants.ERROR_USER_CANCELED);
                }
            });
            background.setRetryButtonText(retryText);
            background.setRetryListener(this);
        }

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
                    case BiometricConstants.ERROR_TIMEOUT:
                        break;
                    case BiometricConstants.ERROR_CANCELED:
                    case BiometricConstants.ERROR_HW_NOT_PRESENT:
                    case BiometricConstants.ERROR_HW_UNAVAILABLE:
                    case BiometricConstants.ERROR_LOCKOUT:
                    case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                    case BiometricConstants.ERROR_NO_BIOMETRICS:
                    case BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL:
                    case BiometricConstants.ERROR_NO_SPACE:
                    case BiometricConstants.ERROR_UNABLE_TO_PROCESS:
                    case BiometricConstants.ERROR_VENDOR:
                        break;
                }
                // if we don't have a background => end auth
                if (background == null) {
                    inProgress = false;
                    reactErrorCallback.invoke(errString, errorCode);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                // if we don't have a background => end auth
                if (background == null) {
                    inProgress = false;
                    reactErrorCallback.invoke(BiometricConstants.ERROR_CANCELED, "Authentication failed");
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                inProgress = false;
                authSuccess = true;
                onAuthSuccess();
            }
        };

        prompt = new BiometricPrompt(activity, executor, callback);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(reason)
                .setConfirmationRequired(true)
                .setDeviceCredentialAllowed(true)
                .build();

        if (background != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.show(activity.getSupportFragmentManager(), "bg");
                }
            });
        }
        showBiometricDialog();
    }

    private void onAuthSuccess() {
        if (isAppActive && authSuccess) {
            if (background != null) {
                background.dismiss();
                background = null;
            }
            reactSuccessCallback.invoke("Successfully authenticated.");
            authSuccess = false;
        }
    }

    private int isFingerprintAuthAvailable() {
        if (getKeyguardManager() != null)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && getKeyguardManager().isKeyguardSecure()) {
                return FingerprintAuthConstants.NOT_SUPPORTED;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getKeyguardManager().isDeviceSecure()) {
                return FingerprintAuthConstants.IS_SUPPORTED;
            }
        return FingerprintAuthConstants.AUTHENTICATION_FAILED;
    }

    @Override
    public void onHostResume() {
        isAppActive = true;
        onAuthSuccess();
    }

    @Override
    public void onHostPause() {
        isAppActive = false;
    }

    @Override
    public void onHostDestroy() {
        isAppActive = false;
    }

    @Override
    public void retry() {
        showBiometricDialog();
    }

    private void showBiometricDialog() {
        final FragmentActivity activity = (FragmentActivity) getCurrentActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    prompt.authenticate(promptInfo);
                }
            });
        }
    }
}
