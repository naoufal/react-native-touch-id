package com.rnfingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
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
import com.facebook.react.module.annotations.ReactModule;

import java.util.concurrent.Executor;

@ReactModule(name = FingerprintAuthModule.NAME)
public class FingerprintAuthModule extends ReactContextBaseJavaModule implements LifecycleEventListener, RetryCallback {
    public static final String NAME = "FingerprintAuth";
    public static final int CONFIRM_DEVICE_CREDENTIAL_CODE = 10001;
    private static final String FRAGMENT_TAG = "fingerprint_dialog";

    private KeyguardManager keyguardManager;

    private boolean inProgress = false;
    private Callback reactSuccessCallback;
    private Callback reactErrorCallback;

    private BiometricBackground background;
    private BiometricPrompt prompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String reason = "Authenticate";
    private int confirmDeviceCredentialCode = CONFIRM_DEVICE_CREDENTIAL_CODE;
    private FragmentActivity launchActivity;

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

    void showDeviceCredentialActivity(Activity activity, String title, String description, int resultCode)
    {
        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(title, description);
        if (intent != null) {
            activity.startActivityForResult(intent, resultCode);
        }
    }

    @TargetApi((Build.VERSION_CODES.M))
    @ReactMethod
    public void deviceCredentialActivityResult(int resultCode) {
        authFinished(resultCode == Activity.RESULT_OK);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @ReactMethod
    public void authenticateOnActivity(final FragmentActivity activity, final String reason, final ReadableMap authConfig, final Callback reactErrorCallback, final Callback reactSuccessCallback) {
        authenticateOnActivity(activity, reason, authConfig, reactErrorCallback, reactSuccessCallback, CONFIRM_DEVICE_CREDENTIAL_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @ReactMethod
    public void authenticateOnActivity(final FragmentActivity activity, final String reason, final ReadableMap authConfig, final Callback reactErrorCallback, final Callback reactSuccessCallback, final int confirmDeviceCredentialCode) {
        if (inProgress || activity == null) {
            return;
        }

        inProgress = true;
        promptInfo = null;
        prompt = null;
        background = null;

        this.reactSuccessCallback = reactSuccessCallback;
        this.reactErrorCallback = reactErrorCallback;
        this.confirmDeviceCredentialCode = confirmDeviceCredentialCode;
        this.reason = reason;

        String cancelText = "Cancel";
        if (authConfig.hasKey("cancelText")) {
            cancelText = authConfig.getString("cancelText");
        }

        String retryText = "Retry";
        if (authConfig.hasKey("retryText")) {
            retryText = authConfig.getString("retryText");
        }

        int availableResult = isFingerprintAuthAvailable();
        boolean isAvailable = availableResult != FingerprintAuthConstants.IS_SUPPORTED;
        if (isAvailable) {
            inProgress = false;
            reactErrorCallback.invoke("Not supported", availableResult);
            return;
        }

        if (authConfig.getBoolean("useBackground")) {
            // Use singleton for avoiding duplication
            background = BiometricBackground.getInstance();
            background.setCancelButtonText(cancelText);

            background.setIsRetryAvailable(true);
            background.setCancelListener(new Callback() {
                @Override
                public void invoke(Object... args) {
                    inProgress = false;
                    launchActivity = null;
                    reactErrorCallback.invoke("User cancelled", BiometricPrompt.ERROR_USER_CANCELED);
                }
            });
            background.setRetryButtonText(retryText);
            background.setRetryListener(this);
        }

        // for biometric authentication (NOT PIN code -> will use device credentials)
        if ((BiometricManager.from(activity).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS)) {
            Executor executor = ContextCompat.getMainExecutor(activity);
            BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Log.e("Fingerprint", "Error: " + errString + " " + errorCode);

                    switch (errorCode) {
                        case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                            // use PIN was pressed
                            return;
                        case BiometricPrompt.ERROR_USER_CANCELED:
                        case BiometricPrompt.ERROR_TIMEOUT:
                        case BiometricPrompt.ERROR_CANCELED:
                        case BiometricPrompt.ERROR_HW_NOT_PRESENT:
                        case BiometricPrompt.ERROR_HW_UNAVAILABLE:
                        case BiometricPrompt.ERROR_LOCKOUT:
                        case BiometricPrompt.ERROR_LOCKOUT_PERMANENT:
                        case BiometricPrompt.ERROR_NO_BIOMETRICS:
                        case BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL:
                        case BiometricPrompt.ERROR_NO_SPACE:
                        case BiometricPrompt.ERROR_UNABLE_TO_PROCESS:
                        case BiometricPrompt.ERROR_VENDOR:
                            break;
                    }
                    // if we don't have a background => end auth
                    if (background == null) {
                        launchActivity = null;
                        inProgress = false;
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    authFinished(false);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    launchActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            authFinished(true);
                        }
                    });
                }
            };

            prompt = new BiometricPrompt(activity, executor, callback);

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(reason)
                    .setConfirmationRequired(true)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (background != null) {
                    background.show(activity.getSupportFragmentManager(), "bg");
                }
            }
        });

        launchActivity = activity;
        showAuthenticationDialog();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @ReactMethod
    public void authenticate(final String reason, final ReadableMap authConfig, final Callback reactErrorCallback, final Callback reactSuccessCallback) {
        final FragmentActivity activity = (FragmentActivity) getCurrentActivity();
        authenticateOnActivity(activity, reason, authConfig, reactErrorCallback, reactSuccessCallback);
    }

    private void authFinished(Boolean success) {
        if (success) {
            inProgress = false;
            launchActivity = null;
            if (background != null) {
                background.dismiss();
                background = null;
            }
            reactSuccessCallback.invoke("Successfully authenticated.");
        } else {
            // if we don't have a background => end auth because no retry
            if (background == null) {
                launchActivity = null;
                inProgress = false;
                reactErrorCallback.invoke("Authentication failed", BiometricPrompt.ERROR_CANCELED);
            }
        }
    }

    // check is any system security presented on device
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
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }

    @Override
    public void retry() {
        showAuthenticationDialog();
    }

    private Boolean shouldShowDeviceCredentialActivity(FragmentActivity activity) {
        final PackageManager pm = activity.getPackageManager();
        boolean advancedSensor = pm.hasSystemFeature(PackageManager.FEATURE_IRIS)
                || pm.hasSystemFeature(PackageManager.FEATURE_FACE);

        // on android 10 & below there is an issue with the fallback PIN with biometric prompt
        // it does result in a authentication error cancelled initially
        return (prompt == null) || (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) && !advancedSensor;
    }

    private void showAuthenticationDialog() {
        if (launchActivity != null) {
            launchActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (shouldShowDeviceCredentialActivity(launchActivity)) {
                        showDeviceCredentialActivity(launchActivity, reason, null, confirmDeviceCredentialCode);
                    } else {
                        prompt.authenticate(promptInfo);
                    }
                }
            });
        }
    }
}
