package com.rnfingerprint;

public class KeyguardHandler {

    public interface Callback {
        void onAuthenticated();

        void onCancelled();
    }
}
