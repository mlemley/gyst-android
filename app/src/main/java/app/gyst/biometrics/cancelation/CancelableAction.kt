package app.gyst.biometrics.cancelation

class CancelableAction constructor(
    internal val signal: android.os.CancellationSignal? = null,
    internal var signalX: androidx.core.os.CancellationSignal? = null
) {

    fun cancel() {
        if (signal?.isCanceled == false)
            signal.cancel()
        if (signalX?.isCanceled == false)
            signalX?.cancel()
    }

}