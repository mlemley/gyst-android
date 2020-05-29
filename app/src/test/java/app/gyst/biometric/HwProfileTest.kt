package app.gyst.biometric

import android.Manifest
import android.app.Application
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.biometric.shadows.ShadowBiometricManager
import app.gyst.biometrics.HwProfile
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowApplication

@RunWith(AndroidJUnit4::class)
class HwProfileTest {
    private fun mockPermissions(
        withBiometricPermission: Boolean = true,
        withFingerPrintPermission: Boolean = true
    ) {
        val shadowApplication: ShadowApplication =
            shadowOf(ApplicationProvider.getApplicationContext<Application>())
        if (withBiometricPermission) {
            shadowApplication.grantPermissions(Manifest.permission.USE_BIOMETRIC)
        }
        if (withFingerPrintPermission) {
            shadowApplication.grantPermissions(Manifest.permission.USE_FINGERPRINT)
        }
    }

    //  NOTE: R introduces a Security level to account for


    //  Current Q and above

    private fun mockBiometrics(
        authenticateResponse: Int = BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        withBiometricPermission: Boolean = true
    ) {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val biometricManager: ShadowBiometricManager =
            Shadow.extract(context.getSystemService(BiometricManager::class.java) as BiometricManager)
        biometricManager.biometricProfileFlag = authenticateResponse
        mockPermissions(withBiometricPermission = withBiometricPermission)
    }

    @Ignore // Robolectric Testing of >= Q requires Java 9
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ShadowBiometricManager::class])
    fun vQ__can_authenticate__biometric_manager__success() {
        mockBiometrics(authenticateResponse = BiometricManager.BIOMETRIC_SUCCESS)

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isTrue()
    }

    @Ignore // Robolectric Testing of >= Q requires Java 9
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ShadowBiometricManager::class])
    fun vQ__requires_permission__current() {
        mockBiometrics(withBiometricPermission = false)

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isFalse()
    }

    @Ignore // Robolectric Testing of >= Q requires Java 9
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ShadowBiometricManager::class])
    fun vQ__can_authenticate__current__no_hardware_availability__cannot() {
        mockBiometrics(authenticateResponse = BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isFalse()
    }

    @Ignore // Robolectric Testing of >= Q requires Java 9
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ShadowBiometricManager::class])
    fun vQ__can_authenticate__current__not_enrolled__can() {
        mockBiometrics(authenticateResponse = BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isTrue()
    }

    @Ignore // Robolectric Testing of >= Q requires Java 9
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q], shadows = [ShadowBiometricManager::class])
    fun vQ__can_authenticate__current__no_hw__cannot() {
        mockBiometrics(authenticateResponse = BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isFalse()
    }


    // Version P

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun vP__can_authenticate__assumes_HW() {
        mockPermissions()
        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isTrue()
    }


    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun vP__requires_manifest_permission() {
        mockPermissions(withBiometricPermission = false)
        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isFalse()
    }


    //  Legacy
    private fun mockFingerprintManger(
        hasHW: Boolean = true,
        hasEnrolledFingerprints: Boolean = true
    ) {
        shadowOf(
            ApplicationProvider.getApplicationContext<Context>()
                .getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        ).apply {
            setIsHardwareDetected(hasHW)
            setHasEnrolledFingerprints(hasEnrolledFingerprints)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun vM__can_authenticate__legacy__no_hardware__cannot() {
        mockPermissions()
        mockFingerprintManger(hasHW = false)

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun vM__can_authenticate__legacy__with_hardware__can() {
        mockPermissions()
        mockFingerprintManger()

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun vM__requires__manifest_permission() {
        mockPermissions(withFingerPrintPermission = false)
        mockFingerprintManger()

        assertThat(
            HwProfile.profileForBuildVersion(ApplicationProvider.getApplicationContext())
                .canAuthenticate()
        ).isFalse()
    }

}