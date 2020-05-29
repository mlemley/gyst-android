package app.gyst.biometrics

import android.content.Context
import app.gyst.biometrics.cancelation.CancelableAction

interface IBiometrics: IHwProfile {

    /**
     * @param context Windowed context for rendering biometric prompt
     * @param observer of Biometric results
     *
     * @return CancelableAction to terminate prompt
     */
    fun promptForAuth(context: Context, observer: BiometricObserver): CancelableAction

}