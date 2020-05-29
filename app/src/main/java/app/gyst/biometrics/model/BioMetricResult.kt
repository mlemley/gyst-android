package app.gyst.biometrics.model

sealed class BioMetricResult {
    object Success : BioMetricResult()
    object Canceled : BioMetricResult()
    data class Error(val error: AuthenticationError) : BioMetricResult()
}

/**
 *  Typed errors from https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt.html#BIOMETRIC_ACQUIRED_PARTIAL
 */
sealed class AuthenticationError {
    /**
     * The Hardware is not available try again later
     */
    object BiometricHwUnavailable : AuthenticationError()

    /**
     * The user does not have biometrics enrolled
     */
    object BiometricNotEnrolled : AuthenticationError()

    /**
     * There is no biometrics hardware
     */
    object BiometricNoHW : AuthenticationError()

    /**
     * Canceled because user attempted to many times
     */
    object BiometricErrorLockout : AuthenticationError()

    /**
     * canceled because Error Lockout occurred to many times
     */
    object BiometricPermanentLockout : AuthenticationError()

    /**
     * not enough storage remaining to complete authentication
     */
    object BiometricNoSpace : AuthenticationError()

    /**
     * The sensor has been running to long
     */
    object BiometricTimeOut : AuthenticationError()

    /**
     * The sensor has been running to long
     */
    object BioMetricUserCanceled : AuthenticationError()

    /**
     * Device does not have a pin
     */
    object NoDeviceCredential : AuthenticationError()


    /**
     * Device does not have a pin
     */
    object BiometricUnableToProcess : AuthenticationError()

    /**
     * Other..
     */
    object BioMetricVendorError : AuthenticationError()

    companion object {
        internal fun fromCode(code: Int): AuthenticationError = when (code) {
            1 -> BiometricHwUnavailable
            2 -> BiometricUnableToProcess
            3 -> BiometricTimeOut
            4 -> BiometricNoSpace
            5 -> BioMetricUserCanceled
            7 -> BiometricErrorLockout
            8 -> BioMetricVendorError
            9 -> BiometricPermanentLockout
            10 -> BioMetricUserCanceled
            11 -> BiometricNotEnrolled
            12 -> BiometricNoHW
            14 -> NoDeviceCredential
            else -> BioMetricVendorError
        }
    }
}


