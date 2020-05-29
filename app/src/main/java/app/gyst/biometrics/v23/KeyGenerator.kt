package app.gyst.biometrics.v23

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException


class KeyGenerator(
    private val keyName:String,
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore"),
    private val keyGenerator: KeyGenerator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
) {


    fun generateKey() {
        try {
            keyStore.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keyName,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )

            keyGenerator.generateKey();

        } catch (exception: Exception) {
            when (exception) {
                is KeyStoreException,
                is NoSuchAlgorithmException,
                is NoSuchProviderException,
                is InvalidAlgorithmParameterException,
                is CertificateException,
                is IOException -> exception.printStackTrace()
                else -> throw exception
            }
        }
    }

    fun obtainCipher(): Cipher? =
        try {
            Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )

        } catch (exception: Exception) {
            when (exception) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException -> {
                    exception.printStackTrace()
                    null
                }
                else -> throw exception
            }
        }

}