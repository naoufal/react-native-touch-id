package com.rnfingerprint;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.facebook.react.bridge.Callback;

public class BiometricBackground extends DialogFragment {

    private ImageView logo;
    private Button retryButton;
    private Button cancelButton;

    private String imageUrl;
    private String cancelText;
    private String retryText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        logo = v.findViewById(R.id.logo);
        Glide.with(requireContext()).load(imageUrl).into(logo);
        cancelButton = v.findViewById(R.id.cancel);
        if (cancelText != null) {
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText(cancelText);
        }
        retryButton = v.findViewById(R.id.retry);
        if (retryText != null) {
            retryButton.setVisibility(View.VISIBLE);
            retryButton.setText(retryText);
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    public void setLogoUrl(String url) {
        imageUrl = url;
    }

    public void setCancelButtonText(String cancel) {
        cancelText = cancel;
    }

    public void setRetryButtonText(String retry) {
        retryText = retry;
    }

    public void setRetryListener(RetryCallback callback) {
        callback.retry();
    }

    public void setCancelListener(Callback reactErrorCallback) {
        reactErrorCallback.invoke("error", FingerprintAuthConstants.AUTHENTICATION_FAILED);
        dismiss();
    }
}
