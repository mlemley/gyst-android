package app.gyst.biometrics.callback

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.model.AuthenticationError
import app.gyst.biometrics.model.BioMetricResult

@RequiresApi(Build.VERSION_CODES.P)
internal class CallbackV28(
    private val observer: BiometricObserver
) : BiometricPrompt.AuthenticationCallback() {

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        AuthenticationError.fromCode(errorCode).let {
            when (it) {
                is AuthenticationError.BioMetricUserCanceled ->
                    observer.onBiometricResult(BioMetricResult.Canceled)
                else ->
                    observer.onBiometricResult(BioMetricResult.Error(it))
            }
        }

    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
        observer.onBiometricResult(BioMetricResult.Success)
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpCode, helpString)
    }

}

