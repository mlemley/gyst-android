package app.gyst.biometrics.cancelation


class SignalProvider {

    fun provide(): android.os.CancellationSignal = android.os.CancellationSignal()
    fun provideX(): androidx.core.os.CancellationSignal = androidx.core.os.CancellationSignal()

}