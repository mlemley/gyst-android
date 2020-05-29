package app.gyst.biometric

import android.os.CancellationSignal
import app.gyst.biometrics.cancelation.CancelableAction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CancellationActionTest {

    @Test
    fun cancels_when_not_canceled() {
        val cancellationSignal:CancellationSignal = mockk(relaxUnitFun = true) {
            every { isCanceled } returns false
        }

        CancelableAction(cancellationSignal).cancel()

        verify {
            cancellationSignal.cancel()
        }
    }

    @Test
    fun does_not_cancel_when_already_canceled() {
        val cancellationSignal:CancellationSignal = mockk(relaxUnitFun = true) {
            every { isCanceled } returns true
        }

        CancelableAction(cancellationSignal).cancel()

        verify(exactly = 0) {
            cancellationSignal.cancel()
        }
    }
}