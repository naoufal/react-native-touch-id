package com.rnfingerprint;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.util.Log;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.InvalidAlgorithmParameterException;
import java.io.IOException;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;


public class FingerprintAuthModule extends ReactContextBaseJavaModule {

  public static boolean inProgress = false;

  public FingerprintAuthModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "FingerprintAuth";
  }

  @ReactMethod
  public void isSupported(Callback reactErrorCallback, Callback reactSuccessCallback) {
    keyguardManager =
            (KeyguardManager) getCurrentActivity().getSystemService(Context.KEYGUARD_SERVICE);
    fingerprintManager =
            (FingerprintManager) getCurrentActivity().getSystemService(Context.FINGERPRINT_SERVICE);
    if(!isFingerprintAuthAvailable()) {
      reactErrorCallback.invoke("Not supported.");
    } else {
      reactSuccessCallback.invoke("Is supported.");
    }
    return ;
  }

  @ReactMethod
  public void authenticate(String reason, ReadableMap authConfig, Callback reactErrorCallback, Callback reactSuccessCallback) {
    if (!inProgress) {
      inProgress = true;
      Activity activity = getCurrentActivity();
      keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
      fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);


      if (isFingerprintAuthAvailable()) {
        generateKey();
        if (cipherInit()) {
          cryptoObject = new FingerprintManager.CryptoObject(cipher);


          DialogResultHandler drh = new DialogResultHandler(reactErrorCallback, reactSuccessCallback);

          fingerprintDialog = new FingerprintDialog(activity, cryptoObject, drh, reason, authConfig);

        }
      }

      return;

    }
  }

  /*** TOUH ID ACTIVITY REALTED STUFF ***/
  private FingerprintDialog fingerprintDialog;

  private FingerprintManager fingerprintManager;
  private KeyguardManager keyguardManager;

  private KeyStore keyStore;
  private KeyGenerator keyGenerator;

  private Cipher cipher;
  private static final String KEY_NAME = "example_key";

  private FingerprintManager.CryptoObject cryptoObject;

  private Context appContext;

  public boolean isFingerprintAuthAvailable() {

      if (android.os.Build.VERSION.SDK_INT < 23) {
          return false;
      }

      if (!keyguardManager.isKeyguardSecure()) {
          return false;
      }

      if (!fingerprintManager.hasEnrolledFingerprints()) {
          return false;
      }

      return true;
  }

  protected void generateKey() {
      try {
          keyStore = KeyStore.getInstance("AndroidKeyStore");
      } catch (Exception e) {
          e.printStackTrace();
      }

      try {
          keyGenerator = KeyGenerator.getInstance(
                  KeyProperties.KEY_ALGORITHM_AES,
                  "AndroidKeyStore");
      } catch (NoSuchAlgorithmException |
              NoSuchProviderException e) {
          throw new RuntimeException(
                  "Failed to get KeyGenerator instance", e);
      }

      try {
          keyStore.load(null);
          keyGenerator.init(new
                  KeyGenParameterSpec.Builder(KEY_NAME,
                  KeyProperties.PURPOSE_ENCRYPT |
                          KeyProperties.PURPOSE_DECRYPT)
                  .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                  .setUserAuthenticationRequired(true)
                  .setEncryptionPaddings(
                          KeyProperties.ENCRYPTION_PADDING_PKCS7)
                  .build());
          keyGenerator.generateKey();
      } catch (NoSuchAlgorithmException |
              InvalidAlgorithmParameterException |
              CertificateException | IOException e) {
          throw new RuntimeException(e);
      }
  }

  public boolean cipherInit() {
      try {
          cipher = Cipher.getInstance(
                  KeyProperties.KEY_ALGORITHM_AES + "/"
                          + KeyProperties.BLOCK_MODE_CBC + "/"
                          + KeyProperties.ENCRYPTION_PADDING_PKCS7);
      } catch (NoSuchAlgorithmException |
              NoSuchPaddingException e) {
          throw new RuntimeException("Failed to get Cipher", e);
      }

      try {
          keyStore.load(null);
          SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                  null);
          cipher.init(Cipher.ENCRYPT_MODE, key);
          return true;
      } catch (KeyPermanentlyInvalidatedException e) {
          return false;
      } catch (KeyStoreException | CertificateException
              | UnrecoverableKeyException | IOException
              | NoSuchAlgorithmException | InvalidKeyException e) {
          throw new RuntimeException("Failed to init Cipher", e);
      }
  }

}
