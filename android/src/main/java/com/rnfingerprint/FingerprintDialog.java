package com.rnfingerprint;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
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
    private DialogResultListener dialogCallback;
    private FingerprintHandler mFingerprintHandler;
    private boolean isAuthInProgress;

    private String authReason;
    private ReadableMap authConfig;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mFingerprintHandler = new FingerprintHandler(context, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);

        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);

        final TextView mFingerprintDescription = (TextView) v.findViewById(R.id.fingerprint_description);
        mFingerprintDescription.setText(authReason);

        final int color = authConfig.getInt("color");
        final ImageView mFingerprintImage = (ImageView) v.findViewById(R.id.fingerprint_icon);
        mFingerprintImage.setColorFilter(color);

        final Button mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setTextColor(color);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelled();
            }
        });

        getDialog().setTitle(authConfig.getString("title"));
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

        if (isAuthInProgress) {
            return;
        }

        isAuthInProgress = true;
        mFingerprintHandler.startAuth(mCryptoObject);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mFingerprintHandler.endAuth();
    }


    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    public void setDialogCallback(DialogResultListener newDialogCallback) {
        dialogCallback = newDialogCallback;
    }

    public void setReasonForAuthentication(String reason) {
        authReason = reason;
    }

    public void setAuthConfig(ReadableMap config) {
        authConfig = config;
    }

    public interface DialogResultListener {
        void onAuthenticated();

        void onError(String errorString);

        void onCancelled();
    }

    @Override
    public void onAuthenticated() {
        isAuthInProgress = false;
        dialogCallback.onAuthenticated();
        dismiss();
    }

    @Override
    public void onError(String errorString) {
        isAuthInProgress = false;
        dialogCallback.onError(errorString);
        dismiss();
    }

    @Override
    public void onCancelled() {
        isAuthInProgress = false;
        mFingerprintHandler.endAuth();
        dialogCallback.onCancelled();
        dismiss();
    }
}
