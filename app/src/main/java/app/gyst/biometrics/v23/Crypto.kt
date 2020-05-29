package app.gyst.biometrics.v23

import android.security.keystore.KeyPermanentlyInvalidatedException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey


class Crypto(
    private val keyName: String,
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore"),
    private val keyGenerator: KeyGenerator = KeyGenerator(keyName)
) {

    fun cipherForAuth(): Cipher? {
        keyGenerator.generateKey()
        return keyGenerator.obtainCipher()?.let { cipher ->
            return try {
                keyStore.load(null)
                val key = keyStore.getKey(keyName, null) as SecretKey
                cipher.init(Cipher.ENCRYPT_MODE, key)
                cipher
            } catch (exception: KeyPermanentlyInvalidatedException) {
                exception.printStackTrace()
                null
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }
        }
    }

}