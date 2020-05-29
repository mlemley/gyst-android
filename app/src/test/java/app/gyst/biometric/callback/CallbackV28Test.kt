package app.gyst.biometric.callback

import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.callback.CallbackV28
import app.gyst.biometrics.model.AuthenticationError.*
import app.gyst.biometrics.model.BioMetricResult
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CallbackV28Test {
    @Test
    fun bubbles_up_auth_errors() {
        val errors = mapOf(
            1 to BiometricHwUnavailable,
            2 to BiometricUnableToProcess,
            3 to BiometricTimeOut,
            4 to BiometricNoSpace,
            5 to BioMetricUserCanceled,
            7 to BiometricErrorLockout,
            8 to BioMetricVendorError,
            9 to BiometricPermanentLockout,
            10 to BioMetricUserCanceled,
            11 to BiometricNotEnrolled,
            12 to BiometricNoHW,
            14 to NoDeviceCredential
        )

        val observer = mockk<BiometricObserver>(relaxUnitFun = true)
        val callback = CallbackV28(observer)

        errors.keys.forEach {
            callback.onAuthenticationError(it, "")
        }

        verify {
            observer.onBiometricResult(BioMetricResult.Error(BiometricHwUnavailable))
            observer.onBiometricResult(BioMetricResult.Error(BiometricUnableToProcess))
            observer.onBiometricResult(BioMetricResult.Error(BiometricTimeOut))
            observer.onBiometricResult(BioMetricResult.Error(BiometricNoSpace))
            observer.onBiometricResult(BioMetricResult.Error(BiometricErrorLockout))
            observer.onBiometricResult(BioMetricResult.Error(BioMetricVendorError))
            observer.onBiometricResult(BioMetricResult.Error(BiometricPermanentLockout))
            observer.onBiometricResult(BioMetricResult.Error(BiometricNotEnrolled))
            observer.onBiometricResult(BioMetricResult.Error(BiometricNoHW))
            observer.onBiometricResult(BioMetricResult.Error(NoDeviceCredential))
        }

        verify(exactly = 2) {
            observer.onBiometricResult(BioMetricResult.Canceled)
        }

        confirmVerified(observer)
    }

    @Test
    fun bubbles_up_success() {
        val observer = mockk<BiometricObserver>(relaxUnitFun = true)
        val callback = CallbackV28(observer)

        callback.onAuthenticationSucceeded(mockk())

        verify {
            observer.onBiometricResult(BioMetricResult.Success)
        }
    }
}