package app.gyst.biometrics

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.os.Build
import androidx.annotation.RequiresApi
import app.gyst.biometrics.ktx.bioMetricsManager
import app.gyst.biometrics.ktx.fingerprintManger
import app.gyst.biometrics.ktx.hasSelfPermission

internal sealed class HwProfile : IHwProfile {

    @RequiresApi(Build.VERSION_CODES.Q)
    internal class Current constructor(val context: Context) : HwProfile() {
        private val hwIndicators = listOf(
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_SUCCESS
        )

        override fun canAuthenticate(): Boolean =
            hwIndicators.contains(context.bioMetricsManager().canAuthenticate())
    }

    internal object LegacyPrompt : HwProfile() {
        override fun canAuthenticate(): Boolean = true
    }

    internal class Legacy constructor(val context: Context) : HwProfile() {
        override fun canAuthenticate(): Boolean = context.fingerprintManger().isHardwareDetected
    }

    internal object UnSupported : HwProfile() {
        override fun canAuthenticate(): Boolean = false
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.P)
        private fun hasBiometricPermission(context: Context): Boolean =
            context.hasSelfPermission(Manifest.permission.USE_BIOMETRIC)

        @RequiresApi(Build.VERSION_CODES.M)
        private fun hasFingerPrintPermission(context: Context): Boolean =
            context.hasSelfPermission(Manifest.permission.USE_FINGERPRINT)

        @SuppressLint("NewApi")
        internal fun profileForBuildVersion(
            context: Context,
            sdkVersion: Int = Build.VERSION.SDK_INT
        ): IHwProfile = when {
            sdkVersion >= Build.VERSION_CODES.Q -> Current(context)
            sdkVersion >= Build.VERSION_CODES.P && !hasBiometricPermission(context) -> UnSupported
            sdkVersion >= Build.VERSION_CODES.P -> LegacyPrompt
            sdkVersion >= Build.VERSION_CODES.M && !hasFingerPrintPermission(context) -> UnSupported
            sdkVersion >= Build.VERSION_CODES.M -> Legacy(context)
            else -> UnSupported
        }

    }
}