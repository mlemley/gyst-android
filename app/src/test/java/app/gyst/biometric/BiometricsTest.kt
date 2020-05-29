package app.gyst.biometric

import android.content.Context
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.Biometrics
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.ktx.asCryptoObject
import app.gyst.biometrics.ktx.fingerprintManger
import app.gyst.biometrics.ui.V23Dialog
import com.google.common.truth.Truth.assertThat
import app.gyst.biometrics.v23.Crypto
import io.mockk.*
import org.junit.Test
import javax.crypto.Cipher

class BiometricsTest {

    // NO BioMetrics

    @Test
    fun noBIO__can_authenticate__false() {
        assertThat(Biometrics.NoBiometrics.canAuthenticate()).isFalse()
    }

    // V23

    private fun createV23(
        fingerprintManagerCompat: FingerprintManagerCompat = mockk {
            every { hasEnrolledFingerprints() } returns true
        },
        cryptoObject: FingerprintManagerCompat.CryptoObject = mockk(relaxed = true),
        cipher: Cipher = mockk(relaxed = true),
        signalProvider: SignalProvider = mockk {
            every { provideX() } returns mockk(relaxed = true)
        }
    ): Biometrics.BiometricAuthenticatorV23 {
        mockkStatic("app.gyst.biometrics.ktx.ExtensionsKt")

        val crypto: Crypto = mockk {
            every { cipherForAuth() } returns cipher
        }

        every { cipher.asCryptoObject() } returns cryptoObject

        val context: Context = mockk {
            every { fingerprintManger() } returns fingerprintManagerCompat
        }

        return Biometrics.BiometricAuthenticatorV23(context, signalProvider, crypto)
    }

    @Test
    fun v23__can_authenticate__true_when_hw_and_fingerprint_enrollment() {
        assertThat(createV23().canAuthenticate()).isTrue()
    }

    @Test
    fun v23__can_authenticate__false_when_hw_and_no_fingerprint_enrollment() {
        val fingerprintManager: FingerprintManagerCompat = mockk {
            every { hasEnrolledFingerprints() } returns false
        }
        assertThat(createV23(fingerprintManager).canAuthenticate()).isFalse()
    }

    @Test
    fun v23__builds_prompt() {
        mockkObject(V23Dialog.Companion)

        every { V23Dialog.show(any(), any(), any(), any(), any()) } returns mockk()

        val cancellationSignal: androidx.core.os.CancellationSignal = mockk()
        val windowedContext: Context = mockk()
        val observer: BiometricObserver = mockk()
        val signalProvider = mockk<SignalProvider> {
            every { provideX() } returns cancellationSignal
        }
        val cryptoObject: FingerprintManagerCompat.CryptoObject = mockk()
        val cipher = mockk<Cipher>(relaxed = true)

        val fingerprintManager: FingerprintManagerCompat = mockk {
            every { hasEnrolledFingerprints() } returns false
        }

        createV23(fingerprintManager, cryptoObject, cipher, signalProvider).promptForAuth(
            windowedContext,
            observer
        )

        verify {
            V23Dialog.show(windowedContext, observer, cryptoObject, any(), signalProvider)
        }
    }

    // V28

    private fun createV28(
        signalProvider: SignalProvider = mockk {
            every { provide() } returns mockk(relaxed = true)
        }
    ): Biometrics.BiometricAuthenticator {
        val context: Context = mockk(relaxed = true)

        return Biometrics.BiometricAuthenticator(context, signalProvider)
    }

    //NOTE: the presence of the biometric prompt is the only way to determine without showing prompt
    @Test
    fun v28__can_authenticate__true() {
        assertThat(createV28().canAuthenticate()).isTrue()
    }

    // V29
}