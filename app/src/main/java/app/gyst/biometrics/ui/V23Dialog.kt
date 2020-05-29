package app.gyst.biometrics.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.VisibleForTesting
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import app.gyst.R
import app.gyst.biometrics.BiometricObserver
import com.google.android.material.bottomsheet.BottomSheetDialog
import app.gyst.biometrics.cancelation.CancelableAction
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.ktx.fingerprintManger
import app.gyst.biometrics.ktx.goneIfEmpty
import app.gyst.biometrics.model.AuthenticationError
import app.gyst.biometrics.model.BioMetricResult


open class V23Dialog constructor(
    context: Context,
    private val observer: BiometricObserver,
    private val signalProvider: SignalProvider,
    private val cancelableAction: CancelableAction,
    private val cryptoObject: FingerprintManagerCompat.CryptoObject
) : BottomSheetDialog(context, R.style.BottomSheetDialog) {

    companion object {
        fun show(
            context: Context,
            observer: BiometricObserver,
            cryptoObject: FingerprintManagerCompat.CryptoObject,
            cancelableAction: CancelableAction,
            signalProvider: SignalProvider
        ) {
            V23Dialog(context, observer, signalProvider, cancelableAction, cryptoObject).show()
        }
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var state: V23State = V23State.Initial
        set(value) {
            val old = field
            field = value
            renderState(old, value)
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val viewBinder: ViewBinder

    init {
        setContentView(LayoutInflater.from(context).inflate(R.layout.v23_auth_dialog, null))
        viewBinder = ViewBinder(delegate).also {
            it.positiveButton?.setOnClickListener {
                when (state) {
                    is V23State.Captured -> performCaptureSuccess()
                    is V23State.Failed,
                    is V23State.Initial -> state = V23State.Capturing
                }
            }

            it.negativeButton?.setOnClickListener { performUserCancel() }
        }

        state = V23State.Initial
    }

    val authCallback = object : FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            if (state is V23State.Capturing) {
                val error = AuthenticationError.fromCode(errMsgId)
                when (error) {
                    is AuthenticationError.BioMetricUserCanceled -> performUserCancel()
                    else -> performCaptureError(error)
                }
            }
        }

        override fun onAuthenticationFailed() {
            state = V23State.Failed
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
            super.onAuthenticationHelp(helpMsgId, helpString)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            state = V23State.Captured
        }

    }

    override fun show() {
        super.show()
        state = V23State.Capturing
    }

    private fun acquireAuth() {
        cancelableAction.signalX = signalProvider.provideX()
        context.fingerprintManger()
            .authenticate(cryptoObject, 0, cancelableAction.signalX, authCallback, null)
    }

    private fun performUserCancel() {
        observer.onBiometricResult(BioMetricResult.Canceled)
        terminateAndDismiss()
    }

    private fun performCaptureError(error: AuthenticationError) {
        observer.onBiometricResult(BioMetricResult.Error(error))
        terminateAndDismiss()
    }

    private fun performCaptureSuccess() {
        observer.onBiometricResult(BioMetricResult.Success)
        terminateAndDismiss()
    }

    private fun terminateAndDismiss() {
        cancelableAction.cancel()
        dismissWithAnimation = true
        dismiss()
    }

    private fun renderState(oldState: V23State, newState: V23State) {
        oldState.transformTo(newState, viewBinder)
        when (state) {
            is V23State.Capturing -> acquireAuth()
            is V23State.Failed -> {
                cancelableAction.cancel()
                viewBinder.stateIcon?.postDelayed({
                    try {
                        state = V23State.Initial
                    } catch (e: Exception) {
                    }
                }, 3000)
            }
        }

        viewBinder.subTitle?.goneIfEmpty()
        viewBinder.description?.goneIfEmpty()
    }
}