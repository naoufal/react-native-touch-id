package com.rnfingerprint;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.String;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.content.DialogInterface;
import android.view.KeyEvent;
import com.facebook.react.bridge.ReadableMap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler.Callback;
import android.view.WindowManager;

public class FingerprintDialog
        implements FingerprintHandler.Callback {

    private Button mCancelButton;
    private View mFingerprintContent;
    private TextView mFingerprintDescription;
    private ImageView mFingerprintImage;

    private FingerprintManager.CryptoObject mCryptoObject;
    private DialogResultListener dialogCallback;
    private FingerprintHandler mFingerprintHandler;

    private ReadableMap authConfig;

    private Context context;
    private Dialog dialog;


    public FingerprintDialog(Context context, FingerprintManager.CryptoObject cryptoObject, DialogResultListener newDialogCallback, String reason, ReadableMap config)
    {
        this.context = context;
        this.mCryptoObject = cryptoObject;
        this.dialogCallback = newDialogCallback;
        this.authConfig = config;

        String title = authConfig.getString("title");
        int color = authConfig.getInt("color");

        dialog = new Dialog(context, R.style.Dialog);
        dialog.setContentView(R.layout.fingerprint_dialog);
        setWidthToDialog(context, dialog, true);

        dialog.setCancelable(false);

        dialog.setTitle(title);

        mFingerprintDescription = (TextView) dialog.findViewById(R.id.fingerprint_description);
        mFingerprintDescription.setText(reason);

        mFingerprintImage = (ImageView) dialog.findViewById(R.id.fingerprint_icon);
        mFingerprintImage.setColorFilter(color);

        mFingerprintHandler = new FingerprintHandler(context, context.getSystemService(FingerprintManager.class), this);

        if (!mFingerprintHandler.isFingerprintAuthAvailable()) {
            dismiss(); //dismiss if fingerpint not available
        } else {
            mFingerprintHandler.startAuth(mCryptoObject);
        }


        mCancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        mCancelButton.setTextColor(color);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFingerprintHandler.endAuth();
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialogCallback.onCancelled();
                    dismiss();
                    return true; // pretend we've processed it
                } else {
                    return false; // pass on to be processed as normal
                }
            }
        });

        try {
            dialog.show();
        } catch (WindowManager.BadTokenException bte) {
            Log.e("ChimeAlertDialog", "Received bad token exception for acivity not running.  Ignoring dialog!");
        }
    }

    public void dismiss()
    {
        if(dialog != null)
        {
            dialog.dismiss();
        }
    }

    public interface DialogResultListener {
      void onAuthenticated();
      void onError(String errorString);
      void onCancelled();
    }

    @Override
    public void onAuthenticated() {
        dialogCallback.onAuthenticated();
        dismiss();
    }

    @Override
    public void onError(String errorString) {
        dialogCallback.onError(errorString);
        dismiss();
    }

    @Override
    public void onCancelled() {
        dialogCallback.onCancelled();
        dismiss();
    }

    public void setWidthToDialog(Context context, Dialog dialog, boolean setDrim)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        dialog.getWindow().setLayout((6 * width)/7, 0);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        if(setDrim)
        {
            params.dimAmount = 0.4f;
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            dialog.getWindow().setAttributes( params );
        }
    }
}
