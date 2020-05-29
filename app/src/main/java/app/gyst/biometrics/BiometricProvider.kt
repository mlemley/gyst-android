package app.gyst.biometrics

import android.content.Context
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.v23.Crypto

class BiometricProvider(
    c: Context,
    private val signalProvider: SignalProvider = SignalProvider(),
    private val crypto: Crypto = Crypto("biometric_key"),
    private val hwProfile: IHwProfile = HwProfile.profileForBuildVersion(c.applicationContext)
) {

    private val context: Context = c.applicationContext

    fun provideBiometrics(): Biometrics = if (hwProfile.canAuthenticate()) {
        when (hwProfile) {
            is HwProfile.Current,
            is HwProfile.LegacyPrompt -> Biometrics.BiometricAuthenticator(context, signalProvider)
            is HwProfile.Legacy -> Biometrics.BiometricAuthenticatorV23(
                context,
                signalProvider,
                crypto
            )
            else -> Biometrics.NoBiometrics
        }
    } else {
        Biometrics.NoBiometrics
    }
}