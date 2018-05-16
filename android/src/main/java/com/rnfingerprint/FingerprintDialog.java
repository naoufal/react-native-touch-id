package com.rnfingerprint;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.String;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.DialogInterface;
import android.view.KeyEvent;
import com.facebook.react.bridge.ReadableMap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler.Callback;

public class FingerprintDialog extends DialogFragment
        implements FingerprintHandler.Callback {

    private Button mCancelButton;
    private View mFingerprintContent;
    private TextView mFingerprintDescription;
    private ImageView mFingerprintImage;

    private FingerprintManager.CryptoObject mCryptoObject;
    private DialogResultListener dialogCallback;
    private FingerprintHandler mFingerprintHandler;

    private String authReason;
    private ReadableMap authConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        getDialog().setTitle(authConfig.getString("title"));
        int color = authConfig.getInt("color");

        setCancelable(false);

        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);

        mFingerprintContent = v.findViewById(R.id.fingerprint_container);

        mFingerprintDescription = (TextView) v.findViewById(R.id.fingerprint_description);

        mFingerprintDescription.setText(authReason);
        mFingerprintImage = (ImageView) v.findViewById(R.id.fingerprint_icon);

        mFingerprintImage.setColorFilter(color);

        mFingerprintHandler = new FingerprintHandler(this.getContext(), this.getActivity().getSystemService(FingerprintManager.class), this);

        if (!mFingerprintHandler.isFingerprintAuthAvailable()) {
            dismissAllowingStateLoss(); //dismiss if fingerpint not available
        } else {
            mFingerprintHandler.startAuth(mCryptoObject);
        }


        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setTextColor(color);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFingerprintHandler.endAuth();
            }
        });

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
         {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
              dialogCallback.onCancelled();
              dismissAllowingStateLoss();
              return true; // pretend we've processed it
            } else {
              return false; // pass on to be processed as normal
            }
          }
        });

        return v;
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
        dialogCallback.onAuthenticated();
        dismissAllowingStateLoss();
    }

    @Override
    public void onError(String errorString) {
        dialogCallback.onError(errorString);
        dismissAllowingStateLoss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
      outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
      super.onSaveInstanceState(outState);
    }

    @Override
    public void onCancelled() {
        dialogCallback.onCancelled();
        dismissAllowingStateLoss();
    }

}
