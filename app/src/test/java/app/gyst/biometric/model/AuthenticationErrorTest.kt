package app.gyst.biometric.model

import app.gyst.biometrics.model.AuthenticationError
import app.gyst.biometrics.model.AuthenticationError.Companion.fromCode
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthenticationErrorTest {
    @Test
    fun creates__error_from_code() {

        assertThat(fromCode(5)).isEqualTo(AuthenticationError.BioMetricUserCanceled)
        assertThat(fromCode(12)).isEqualTo(AuthenticationError.BiometricNoHW)
        assertThat(fromCode(1)).isEqualTo(AuthenticationError.BiometricHwUnavailable)
        assertThat(fromCode(7)).isEqualTo(AuthenticationError.BiometricErrorLockout)
        assertThat(fromCode(9)).isEqualTo(AuthenticationError.BiometricPermanentLockout)
        assertThat(fromCode(11)).isEqualTo(AuthenticationError.BiometricNotEnrolled)
        assertThat(fromCode(14)).isEqualTo(AuthenticationError.NoDeviceCredential)
        assertThat(fromCode(4)).isEqualTo(AuthenticationError.BiometricNoSpace)
        assertThat(fromCode(3)).isEqualTo(AuthenticationError.BiometricTimeOut)
        assertThat(fromCode(2)).isEqualTo(AuthenticationError.BiometricUnableToProcess)
        assertThat(fromCode(10)).isEqualTo(AuthenticationError.BioMetricUserCanceled)
        assertThat(fromCode(8)).isEqualTo(AuthenticationError.BioMetricVendorError)
    }
}