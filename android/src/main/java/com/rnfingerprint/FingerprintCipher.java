package com.rnfingerprint;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by Nikolay Demyankov on 24.05.18.
 */
public class FingerprintCipher {

    private static final String KEY_NAME = "example_key";

    private Cipher cipher;

    @TargetApi(Build.VERSION_CODES.M)
    public Cipher getCipher() {
        if (cipher != null) {
            return cipher;
        }

        try {
            final KeyStore keyStore = generateKey();
            final String algo = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
            cipher = Cipher.getInstance(algo);

            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            // ignored
        }

        return cipher;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private KeyStore generateKey() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        keyStore.load(null);
        keyGenerator.init(new KeyGenParameterSpec.Builder(
                KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
        keyGenerator.generateKey();

        return keyStore;
    }
}
