package com.rnfingerprint;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class KeyguardDialog extends DialogFragment implements KeyguardHandler.Callback{
    private KeyguardManager mKeyguardManager;
    private KeyguardResultListener keyguardCallback;
    private int PROMPT_FOR_KEYGUARD = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mKeyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.keyguard_dialog, container, false);

        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        startActivityForResult(intent, PROMPT_FOR_KEYGUARD);

        return v;
    }

    public void setDialogCallback(KeyguardResultListener newDialogCallback) {
        this.keyguardCallback = newDialogCallback;
    }

    public interface KeyguardResultListener {
        void onAuthenticated();

        void onCancelled();

    }

    @Override
    public void onAuthenticated() {
        this.keyguardCallback.onAuthenticated();
        dismiss();
    }

    @Override
    public void onCancelled() {
        this.keyguardCallback.onCancelled();
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
}
