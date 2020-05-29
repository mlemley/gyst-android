package app.gyst.biometric.v23

import android.security.keystore.KeyPermanentlyInvalidatedException
import app.gyst.biometrics.v23.Crypto
import app.gyst.biometrics.v23.KeyGenerator
import com.google.common.truth.Truth.assertThat
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.Test
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey

const val KeyStoreName: String = "keystore-name"

class CryptoTest {

    private fun createCrpyto(
        keystore: KeyStore = mockk(relaxed = true),
        keyGenerator: KeyGenerator = mockk(relaxed = true)
    ): Crypto = Crypto(KeyStoreName, keystore, keyGenerator)


    @Test
    fun cipher__obtains_secret_key_for_auth() {
        val secretKey: SecretKey = mockk()
        val cipher: Cipher = mockk(relaxed = true)
        val keystore: KeyStore = mockk(relaxUnitFun = true) {
            every { getKey(KeyStoreName, null) } returns secretKey
        }
        val generator = mockk<KeyGenerator>(relaxUnitFun = true) {
            every { obtainCipher() } returns cipher
        }
        val crypto = createCrpyto(keystore, generator)

        assertThat(crypto.cipherForAuth()).isEqualTo(cipher)

        verifyOrder {
            generator.generateKey()
            generator.obtainCipher()
            keystore.load(null)
            keystore.getKey(KeyStoreName, null)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }

        confirmVerified(generator, cipher, keystore)

    }

    @Test
    fun cipher__null_when_failed_to_load() {
        val cipher: Cipher = mockk(relaxed = true)
        val keystore: KeyStore = mockk(relaxUnitFun = true) {
            every { getKey(KeyStoreName, null) } throws KeyPermanentlyInvalidatedException()
        }
        val generator = mockk<KeyGenerator>(relaxUnitFun = true) {
            every { obtainCipher() } returns cipher
        }
        val crypto = createCrpyto(keystore, generator)

        assertThat(crypto.cipherForAuth()).isNull()
    }
}