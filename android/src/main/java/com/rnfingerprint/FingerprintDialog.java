package com.rnfingerprint;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.react.bridge.ReadableMap;

public class FingerprintDialog extends DialogFragment implements FingerprintHandler.Callback {

    private FingerprintManager.CryptoObject mCryptoObject;
    private KeyguardManager mKeyguardManager;
    private DialogResultListener dialogCallback;
    private FingerprintHandler mFingerprintHandler;
    private boolean isAuthInProgress;

    private ImageView mFingerprintImage;
    private TextView mFingerprintSensorDescription;
    private TextView mFingerprintError;

    private String authReason;
    private int imageColor = 0;
    private int imageErrorColor = 0;
    private String dialogTitle = "";
    private String cancelText = "";
    private String sensorDescription = "";
    private String sensorErrorDescription = "";
    private String errorText = "";
    private int failedTimes;
    private int oneTimeOfFailure = 1;
    private int maxTimesCanFail = 5;
    private int PROMPT_FOR_KEYGUARD = 1;
    private Button mEnterKeyguardButton;
    private boolean usePasscodeFallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mFingerprintHandler = new FingerprintHandler(context, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        setCancelable(false);
        this.mKeyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        this.failedTimes = 0;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);

        final TextView mFingerprintDescription = (TextView) v.findViewById(R.id.fingerprint_description);
        mFingerprintDescription.setText(this.authReason);

        this.mFingerprintImage = (ImageView) v.findViewById(R.id.fingerprint_icon);
        if (this.imageColor != 0) {
            this.mFingerprintImage.setColorFilter(this.imageColor);
        }

        this.mFingerprintSensorDescription = (TextView) v.findViewById(R.id.fingerprint_sensor_description);
        this.mFingerprintSensorDescription.setText(this.sensorDescription);

        this.mFingerprintError = (TextView) v.findViewById(R.id.fingerprint_error);
        this.mFingerprintError.setText(this.errorText);

        this.mEnterKeyguardButton = (Button) v.findViewById(R.id.enter_keyguard_button);
        this.mEnterKeyguardButton.setVisibility(v.INVISIBLE);
        this.mEnterKeyguardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptForKeyguard();
            }
        });

        final Button mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setText(this.cancelText);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelled();
            }
        });

        getDialog().setTitle(this.dialogTitle);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_BACK || mFingerprintHandler == null) {
                    return false; // pass on to be processed as normal
                }
                onCancelled();
                return true; // pretend we've processed it
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.isAuthInProgress) {
            return;
        }

        this.isAuthInProgress = true;
        this.mFingerprintHandler.startAuth(mCryptoObject);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.isAuthInProgress) {
            this.mFingerprintHandler.endAuth();
            this.isAuthInProgress = false;
        }
    }

    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        this.mCryptoObject = cryptoObject;
    }

    public void setDialogCallback(DialogResultListener newDialogCallback) {
        this.dialogCallback = newDialogCallback;
    }

    public void setReasonForAuthentication(String reason) {
        this.authReason = reason;
    }

    public void setAuthConfig(final ReadableMap config) {
        if (config == null) {
            return;
        }

        if (config.hasKey("title")) {
            this.dialogTitle = config.getString("title");
        }

        if (config.hasKey("cancelText")) {
            this.cancelText = config.getString("cancelText");
        }

        if (config.hasKey("sensorDescription")) {
            this.sensorDescription = config.getString("sensorDescription");
        }

        if (config.hasKey("sensorErrorDescription")) {
            this.sensorErrorDescription = config.getString("sensorErrorDescription");
        }

        if (config.hasKey("imageColor")) {
            this.imageColor = config.getInt("imageColor");
        }

        if (config.hasKey("imageErrorColor")) {
            this.imageErrorColor = config.getInt("imageErrorColor");
        }

        if (config.hasKey("passcodeFallback")) {
            this.usePasscodeFallback = config.getBoolean("passcodeFallback");
        }
    }

    public interface DialogResultListener {
        void onAuthenticated();

        void onError(String errorString, int errorCode);

        void onCancelled();

    }

    @Override
    public void onAuthenticated() {
        this.failedTimes = 0;
        this.isAuthInProgress = false;
        this.dialogCallback.onAuthenticated();
        dismiss();
    }

    @Override
    public void onError(String errorString, int errorCode) {
        this.failedTimes++;
        if (this.usePasscodeFallback && this.failedTimes >= this.oneTimeOfFailure) {
            this.mEnterKeyguardButton.setVisibility(getView().VISIBLE);
            if (this.failedTimes == this.maxTimesCanFail) {
                promptForKeyguard();
            }
        }
        this.mFingerprintError.setText(errorString);
        this.mFingerprintImage.setColorFilter(this.imageErrorColor);
        this.mFingerprintSensorDescription.setText(this.sensorErrorDescription);
    }

    @Override
    public void onCancelled() {
        this.failedTimes = 0;
        this.isAuthInProgress = false;
        this.mFingerprintHandler.endAuth();
        this.dialogCallback.onCancelled();
        dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            onAuthenticated();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            onCancelled();
        }
    }

    public void promptForKeyguard() {
        Intent intent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        startActivityForResult(intent, PROMPT_FOR_KEYGUARD);
    }
}
