package app.gyst.biometric.ui

import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.R
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.cancelation.CancelableAction
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.ui.V23Dialog
import app.gyst.biometrics.ui.V23State
import app.gyst.biometrics.ui.ViewBinder
import com.google.common.truth.Truth.assertThat
import io.mockk.*

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class V23StateTest {
    private fun createScenario(
        biometricObserver: BiometricObserver = mockk(relaxed = true),
        signalProvider: SignalProvider = mockk(relaxed = true),
        cancelableAction: CancelableAction = mockk(relaxed = true),
        cryptoObject: FingerprintManagerCompat.CryptoObject = mockk(relaxed = true)
    ): ActivityScenario<TestableActivity> {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        Shadows.shadowOf(applicationContext.packageManager).apply {
            addOrUpdateActivity(ActivityInfo().apply {
                name = TestableActivity::class.java.name
                packageName = applicationContext.packageName
            })
        }
        return ActivityScenario.launch(TestableActivity::class.java).onActivity {
            V23Dialog.show(
                it,
                biometricObserver,
                cryptoObject,
                cancelableAction,
                signalProvider
            )
        }
    }

    private fun dialog(): V23Dialog {
        val latestDialog = ShadowDialog.getLatestDialog()
        assertThat(latestDialog).isNotNull()
        assertThat(latestDialog.isShowing).isTrue()

        return latestDialog as V23Dialog
    }

    private fun viewBinder() = ViewBinder(dialog().delegate)

    // Assertions
    private fun assertInitState(viewBinder: ViewBinder) {

        with(viewBinder.stateIcon!!) {
            assertThat(Shadows.shadowOf(drawable).createdFromResId).isEqualTo(
                R.drawable.fingerprint
            )
            assertThat(colorFilter).isNull()
            assertThat(tag).isNull()
        }


        assertThat(viewBinder.helpMessage?.visibility).isEqualTo(View.INVISIBLE)

        with(viewBinder.positiveButton!!) {
            assertThat(text).isEqualTo(context.getText(R.string.biometric_retry))
            assertThat(isEnabled).isTrue()
        }
    }

    private fun assertCapturingState(viewBinder: ViewBinder) {
        with(viewBinder.stateIcon!!) {
            assertThat(Shadows.shadowOf(drawable).createdFromResId).isEqualTo(
                R.drawable.fingerprint
            )
            val animator = tag as ValueAnimator
            assertThat(animator.duration).isEqualTo(500)
            assertThat(animator.repeatMode).isEqualTo(ValueAnimator.REVERSE)
            //assertThat(animator.repeatCount).isEqualTo(-1)
            assertThat(animator.isStarted).isTrue()
        }

        assertThat(viewBinder.helpMessage?.visibility).isEqualTo(View.INVISIBLE)

        with(viewBinder.positiveButton!!) {
            assertThat(text).isEqualTo(context.getText(R.string.biometric_prompt_positive_button_text))
            assertThat(isEnabled).isFalse()
        }
    }

    private fun assertCapturedState(viewBinder: ViewBinder) {
        with(viewBinder.stateIcon!!) {
            assertThat(Shadows.shadowOf(drawable).createdFromResId).isEqualTo(
                R.drawable.fingerprint
            )
        }

        assertThat(viewBinder.helpMessage?.visibility).isEqualTo(View.INVISIBLE)

        with(viewBinder.positiveButton!!) {
            assertThat(text).isEqualTo(context.getText(R.string.biometric_prompt_positive_button_text))
            assertThat(isEnabled).isTrue()
        }
    }

    private fun assertFailedState(viewBinder: ViewBinder) {
        with(viewBinder.stateIcon!!) {
            assertThat(Shadows.shadowOf(drawable).createdFromResId).isEqualTo(
                R.drawable.error
            )
            assertThat(colorFilter).isNotNull()
            assertThat(tag).isNull()
        }

        with(viewBinder.helpMessage!!) {
            assertThat(visibility).isEqualTo(View.VISIBLE)
            assertThat(currentTextColor).isEqualTo(
                resources.getColor(
                    R.color.biometric_capture_error,
                    context.theme
                )
            )
            assertThat(text).isEqualTo(resources.getString(R.string.biometric_auth_failure_message))
        }

        with(viewBinder.positiveButton!!) {
            assertThat(text).isEqualTo(context.getText(R.string.biometric_retry))
            assertThat(isEnabled).isTrue()
        }
    }

    // Initial State

    @Test
    fun initial__rendering() {
        createScenario().onActivity { activity ->
            V23State.Capturing.transformTo(V23State.Initial, viewBinder())

            assertInitState(viewBinder())
        }.close()
    }

    @Test
    fun initial__transforms_to_next() {
        createScenario().onActivity { activity ->
            val viewBinder = viewBinder()
            V23State.Capturing.transformTo(V23State.Initial, viewBinder)
            assertInitState(viewBinder)

            V23State.Initial.transformTo(V23State.Capturing, viewBinder)

            assertCapturingState(viewBinder)

        }.close()
    }

    // Capturing State

    @Test
    fun capturing__rendering() {
        createScenario().onActivity { activity ->
            V23State.Capturing.render(viewBinder())

            assertCapturingState(viewBinder())
        }.close()
    }

    @Test
    fun capturing__transforms_to_error() {
        createScenario().onActivity { activity ->
            val viewBinder = viewBinder()
            val animator = mockk<ValueAnimator>(relaxed = true)
            V23State.Capturing.render(viewBinder)
            assertCapturingState(viewBinder)
            viewBinder.stateIcon?.tag = animator

            V23State.Capturing.transformTo(V23State.Failed, viewBinder)

            assertFailedState(viewBinder)

            verify {
                animator.cancel()
            }
        }.close()
    }

    // Capturing State

    @Test
    fun failed__rendering() {
        createScenario().onActivity { activity ->
            val viewBinder = viewBinder()
            V23State.Capturing.render(viewBinder())

            V23State.Capturing.transformTo(V23State.Failed, viewBinder)

            assertFailedState(viewBinder)
        }.close()
    }

    @Test
    fun failed__transforms_to_initial() {
        createScenario().onActivity { activity ->
            val viewBinder = viewBinder()
            V23State.Capturing.render(viewBinder())

            V23State.Capturing.transformTo(V23State.Failed, viewBinder)
            assertFailedState(viewBinder)

            V23State.Failed.transformTo(V23State.Initial, viewBinder)
            assertInitState(viewBinder)
        }.close()
    }

    // Confirmed State

    @Test
    fun confirmed__rendering() {
        createScenario().onActivity { activity ->
            val viewBinder = viewBinder()
            V23State.Captured.render(viewBinder)

            assertCapturedState(viewBinder)
        }.close()
    }
}