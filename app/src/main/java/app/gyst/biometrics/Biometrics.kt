package app.gyst.biometrics

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import app.gyst.R
import app.gyst.biometrics.callback.CallbackV28
import app.gyst.biometrics.cancelation.CancelableAction
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.ktx.asCryptoObject
import app.gyst.biometrics.ktx.fingerprintManger
import app.gyst.biometrics.ui.V23Dialog
import app.gyst.biometrics.v23.Crypto

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
sealed class Biometrics : IBiometrics, IHwProfile {

    internal object NoBiometrics : Biometrics() {

        override fun canAuthenticate(): Boolean = false

        override fun promptForAuth(
            context: Context,
            observer: BiometricObserver
        ): CancelableAction = throw IllegalStateException("Can not prompt with no Biometrics")
    }

    internal class BiometricAuthenticator(
        private val context: Context,
        private val signalProvider: SignalProvider
    ) : Biometrics() {

        @RequiresApi(Build.VERSION_CODES.P)
        override fun promptForAuth(
            context: Context,
            observer: BiometricObserver
        ): CancelableAction {
            val cancellationSignal = signalProvider.provide()
            val callback = CallbackV28(observer)
            BiometricPrompt.Builder(context)
                .setTitle(context.getText(R.string.biometric_prompt_title))
                .setSubtitle(context.getText(R.string.biometric_prompt_sub_title))
                .setDescription(context.getText(R.string.biometric_prompt_description))

                .setNegativeButton(context.getText(R.string.biometric_prompt_negative_button_text),
                    context.mainExecutor,
                    DialogInterface.OnClickListener { _, _ ->
                        callback.onAuthenticationError(10, "Authentication Canceled")
                    }
                ).build()
                .authenticate(cancellationSignal, context.mainExecutor, callback)
            return CancelableAction(cancellationSignal)
        }

        override fun canAuthenticate(): Boolean = true
    }

    internal class BiometricAuthenticatorV23(
        private val context: Context,
        private val signalProvider: SignalProvider,
        private val crypto: Crypto
    ) : Biometrics() {

        override fun canAuthenticate(): Boolean =
            context.fingerprintManger().hasEnrolledFingerprints()

        override fun promptForAuth(
            context: Context,
            observer: BiometricObserver
        ): CancelableAction {
            val cancellationSignal = signalProvider.provideX()
            val cancelableAction = CancelableAction(signalX = cancellationSignal)

            crypto.cipherForAuth()?.let { cipher ->
                V23Dialog.show(context, observer, cipher.asCryptoObject(), cancelableAction, signalProvider)
            }

            return cancelableAction
        }
    }

}
