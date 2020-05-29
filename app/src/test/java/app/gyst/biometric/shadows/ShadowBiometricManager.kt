package app.gyst.biometric.shadows


import android.Manifest
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import androidx.annotation.IntDef
import androidx.annotation.RequiresPermission
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowBuild

/** Provides testing APIs for [BiometricManager]  */
@Implements(
    className = "android.hardware.biometrics.BiometricManager",
    minSdk = ShadowBuild.Q,
    isInAndroidSdk = false
)
class ShadowBiometricManager {
    /** Possible result for [BiometricManager.canAuthenticate]  */
    @IntDef(
        BiometricManager.BIOMETRIC_SUCCESS,
        BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    )
    internal annotation class BiometricError

    var biometricProfileFlag:Int = BiometricManager.BIOMETRIC_SUCCESS

    @RequiresPermission(Manifest.permission.USE_BIOMETRIC)
    @Implementation
    @BiometricError
    protected fun canAuthenticate(): Int = biometricProfileFlag

}