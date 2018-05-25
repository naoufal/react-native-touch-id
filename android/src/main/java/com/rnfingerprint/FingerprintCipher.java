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
 * Helper to create a Cipher.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintCipher {

    private static final String KEY_NAME = "example_key";
    private static final String CIPHER_ALGO = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;

    private Cipher cipher;

    public Cipher getCipher() {
        if (cipher != null) {
            return cipher;
        }

        try {
            final KeyStore keyStore = generateKey();
            cipher = Cipher.getInstance(CIPHER_ALGO);

            keyStore.load(null);
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(KEY_NAME, null));
        } catch (Exception e) {
            // nothing we can do about it, return null
        }

        return cipher;
    }

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
