package app.gyst.biometric

import android.content.Context
import app.gyst.biometrics.BiometricProvider
import app.gyst.biometrics.Biometrics
import app.gyst.biometrics.HwProfile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class BiometricProviderTest {

    private fun mockContext(): Context = mockk(relaxed = true) {
        every { applicationContext } returns this
    }

    private fun createProvider(
        context: Context = mockContext(),
        hwProfile: HwProfile = mockk(relaxUnitFun = true)
    ): BiometricProvider = BiometricProvider(context, mockk(), mockk(), hwProfile)

    @Test
    fun provides__no_biometrics_when_support_not_enabled() {
        val profile = mockk<HwProfile> {
            every { canAuthenticate() } returns false
        }

        val provider = createProvider(hwProfile = profile)

        assertThat(provider.provideBiometrics()).isInstanceOf(Biometrics.NoBiometrics::class.java)
    }

    @Test
    fun returns_v23__for_hardware_profile() {
        val profile: HwProfile.Legacy = mockk(relaxed = true) {
            every { canAuthenticate() } returns true
        }

        val provider = createProvider(hwProfile = profile)

        assertThat(provider.provideBiometrics()).isInstanceOf(Biometrics.BiometricAuthenticatorV23::class.java)

    }

    @Test
    fun returns_Current__for_LegacyPrompt__hardware_profile() {
        val profile = mockk<HwProfile.LegacyPrompt>(relaxed = true){
            every { canAuthenticate() } returns true
        }

        val provider = createProvider(hwProfile = profile)

        assertThat(provider.provideBiometrics()).isInstanceOf(Biometrics.BiometricAuthenticator::class.java)

    }

    @Test
    fun returns_Current__for_Current__hardware_profile() {
        val profile = mockk<HwProfile.Current>(relaxed = true) {
            every { canAuthenticate() } returns true
        }

        val provider = createProvider(hwProfile = profile)

        assertThat(provider.provideBiometrics()).isInstanceOf(Biometrics.BiometricAuthenticator::class.java)

    }
}