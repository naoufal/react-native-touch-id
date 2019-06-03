package com.rnfingerprint;

import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.KeyEvent;
import com.facebook.react.bridge.ReadableMap;
import android.hardware.fingerprint.FingerprintManager;



public class FingerprintDialog extends DialogFragment implements FingerprintHandler.Callback {

    private FingerprintManager.CryptoObject mCryptoObject;
    private DialogResultListener dialogCallback;
    private FingerprintHandler mFingerprintHandler;
    private boolean isAuthInProgress;

    private ImageView mFingerprintBackground;
    private ImageView mFingerprintImage;
    private TextView mFingerprintSensorDescription;
    //private TextView mFingerprintError;
    private Button mCancelButton;
    private Button mUsePassword;

    private String authReason;
    private int imageColor = 0;
    private int imageErrorColor = 0;
    private String dialogTitle = "";
    private String cancelText = "";
    private String usePassword = "";
    private String sensorDescription = "";
    private String sensorErrorDescription = "";
    private String errorText = "";

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fingerprint_dialog, container, false);

        final TextView mFingerprintDescription =  view.findViewById(R.id.fingerprint_description);
        mFingerprintDescription.setText(this.authReason);

        this.mFingerprintImage = view.findViewById(R.id.fingerprint_icon);
        this.mFingerprintBackground = view.findViewById(R.id.fingerprint_background);
        this.mFingerprintSensorDescription = view.findViewById(R.id.fingerprint_sensor_description);
        //this.mFingerprintError = view.findViewById(R.id.fingerprint_error);
        this.mCancelButton = view.findViewById(R.id.cancel_button);
        this.mUsePassword = view.findViewById(R.id.use_password);


        this.mFingerprintImage.setImageResource(R.drawable.ic_fp_40px);
        this.mFingerprintBackground.setImageResource(R.drawable.rounded_shape);
        this.mFingerprintSensorDescription.setText(this.sensorDescription);
        //this.mFingerprintError.setText(this.errorText);

        if (this.imageColor != 0) {
            this.mFingerprintImage.setColorFilter(this.imageColor);
        }

        mCancelButton.setText(this.cancelText);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelled();
            }
        });

        mUsePassword.setText(this.usePassword);
        mUsePassword.setOnClickListener(new View.OnClickListener() {
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

        return view;
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

        if (config.hasKey("usePassword")) {
            this.usePassword = config.getString("usePassword");
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
    }

    public interface DialogResultListener {
        void onAuthenticated();

        void onError(String errorString, int errorCode);

        void onCancelled();
    }

    @Override
    public void startedAuthentication() {

    }

    @Override
    public void onAuthenticated() {
        this.isAuthInProgress = false;

        this.setUserResponse(
                "Fingerprint recognized",
                "#009688",
                R.drawable.round_done_white,
                R.drawable.rounded_shape_success);

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                FingerprintDialog.this.dialogCallback.onAuthenticated();
                dismiss();
            }
        };
        handler.postDelayed(r, 2000);

    }

    @Override
    public void onError(String errorString, int errorCode) {
        this.setUserResponse(
                errorString,
                "#f4511e",
                R.drawable.round_error_white,
                R.drawable.rounded_shape_error);

        ObjectAnimator
                .ofFloat(this.mFingerprintSensorDescription, "translationX", 0, 8, -8, 8, -8, 8, -8, 8, -8, 0)
                .setDuration(1000)
                .start();
    }

    @Override
    public void onCancelled() {
        this.isAuthInProgress = false;
        this.mFingerprintHandler.endAuth();
        this.dialogCallback.onCancelled();
        dismiss();
    }

    private void setUserResponse(String message, String textColor, int fingerprintImageId, int fingerprintbackgroundId){
        this.mFingerprintSensorDescription.setText(message);
        this.mFingerprintSensorDescription.setTextColor(Color.parseColor(textColor));
        this.mFingerprintImage.setImageResource(fingerprintImageId);
        this.mFingerprintBackground.setImageResource(fingerprintbackgroundId);
    }
}
