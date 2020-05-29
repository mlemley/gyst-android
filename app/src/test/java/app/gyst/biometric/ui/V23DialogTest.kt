package app.gyst.biometric.ui

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.R
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.cancelation.CancelableAction
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.ktx.show
import app.gyst.biometrics.model.AuthenticationError
import app.gyst.biometrics.model.BioMetricResult
import app.gyst.biometrics.ui.V23Dialog
import app.gyst.biometrics.ui.V23State
import com.google.android.material.button.MaterialButton
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import kotlinx.android.synthetic.main.v23_auth_dialog.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper


class TestableActivity : AppCompatActivity() {
}

@RunWith(AndroidJUnit4::class)
class V23DialogTest {

    private fun createScenario(
        biometricObserver: BiometricObserver = mockk(relaxed = true),
        signalProvider: SignalProvider = mockk(relaxed = true),
        cancelableAction: CancelableAction = mockk(relaxed = true),
        cryptoObject: FingerprintManagerCompat.CryptoObject = mockk(relaxed = true)
    ): ActivityScenario<TestableActivity> {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        shadowOf(applicationContext.packageManager).apply {
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

    @Test
    fun removes_space_allocation_for_empty_values() {
        createScenario().onActivity {
            val dialog = dialog()
            dialog.viewBinder.subTitle?.apply {
                text = "--subtitle--"
                show()
            }
            dialog.viewBinder.description?.apply {
                show()
                text = "--description--"
            }

            dialog.state = V23State.Initial

            assertThat(dialog.subtitle.visibility).isEqualTo(View.VISIBLE)
            assertThat(dialog.description.visibility).isEqualTo(View.VISIBLE)
        }.close()

        createScenario().onActivity {
            val dialog = dialog()
            dialog.viewBinder.subTitle?.apply {
                text = ""
                show()
            }
            dialog.viewBinder.description?.apply {
                show()
                text = ""
            }

            dialog.state = V23State.Capturing

            assertThat(dialog.subtitle.visibility).isEqualTo(View.GONE)
            assertThat(dialog.description.visibility).isEqualTo(View.GONE)
        }.close()
    }

    @Test
    fun captures_auth__when_shown() {
        createScenario().onActivity {
            assertThat(dialog().state).isEqualTo(V23State.Capturing)
        }.close()
    }

    @Test
    fun failed_state_offers_retry() {
        createScenario().onActivity {
            val dialog = dialog()
            dialog.state = V23State.Failed

            dialog.biometric_positive_button.performClick()

            assertThat(dialog.state).isEqualTo(V23State.Capturing)
        }.close()
    }

    @Test
    fun failed_automatically_cycles_to_init_state() {
        createScenario().onActivity {
            val dialog = dialog()
            dialog.state = V23State.Failed

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            assertThat(dialog.state).isEqualTo(V23State.Initial)
        }.close()
    }

// V23State Transitions

    @Test
    fun invalid_auth__renders_error() {
        createScenario().onActivity {
            val dialog = dialog()

            dialog.authCallback.onAuthenticationFailed()

            assertThat(dialog.state).isEqualTo(V23State.Failed)

        }.close()
    }

    @Test
    fun valid_auth__moves_to_captured() {
        val observer: BiometricObserver = mockk(relaxed = true)
        createScenario(observer).onActivity {
            val dialog = dialog()

            dialog.authCallback.onAuthenticationSucceeded(
                FingerprintManagerCompat.AuthenticationResult(
                    mockk()
                )
            )

            assertThat(dialog.isShowing).isTrue()
            assertThat(dialog.state).isEqualTo(V23State.Captured)
        }.close()
    }

    @Test
    fun captured__positive_button__emits_success() {
        val observer: BiometricObserver = mockk(relaxed = true)
        createScenario(observer).onActivity {
            val dialog = dialog()
            dialog.state = V23State.Captured

            dialog.biometric_positive_button.performClick()

            verify {
                observer.onBiometricResult(BioMetricResult.Success)
                assertThat(dialog.isShowing).isFalse()
            }

        }.close()
    }

    @Test
    fun auth__error__only_bubbles_up_when_capturing() {
        val observer: BiometricObserver = mockk(relaxed = true)
        val cancelableAction: CancelableAction = mockk(relaxed = true)
        createScenario(observer, cancelableAction = cancelableAction).onActivity {
            val dialog = dialog()
            dialog.state = V23State.Failed

            dialog.authCallback.onAuthenticationError(5, "User Canceled")

            assertThat(dialog.isShowing).isTrue()
            verify(exactly = 0) { observer.onBiometricResult(any()) }
        }.close()
    }

    @Test
    fun auth__error__bubbles__when_capturing() {
        val observer: BiometricObserver = mockk(relaxed = true)
        createScenario(observer).onActivity {
            val dialog = dialog()

            dialog.authCallback.onAuthenticationError(4, "No Space")

            assertThat(dialog.isShowing).isFalse()
            verify { observer.onBiometricResult(BioMetricResult.Error(AuthenticationError.fromCode(4))) }
        }.close()
    }

    @Test
    fun cancel_code__cancels_and_dismisses() {
        val observer: BiometricObserver = mockk(relaxed = true)
        val cancelableAction: CancelableAction = mockk(relaxed = true)
        createScenario(observer, cancelableAction = cancelableAction).onActivity {
            val dialog = dialog()

            dialog.authCallback.onAuthenticationError(10, "No Space")

            assertThat(dialog.isShowing).isFalse()
            verify {
                observer.onBiometricResult(BioMetricResult.Canceled)
                cancelableAction.cancel()
            }
        }.close()
    }

    @Test
    fun cancel_button__cancels_and_dismisses() {
        val observer: BiometricObserver = mockk(relaxed = true)
        val cancelableAction: CancelableAction = mockk(relaxed = true)
        createScenario(observer, cancelableAction = cancelableAction).onActivity {
            val dialog = dialog()
            dialog.findViewById<MaterialButton>(R.id.biometric_negative_button)?.performClick()

            assertThat(dialog.isShowing).isFalse()
            verify {
                observer.onBiometricResult(BioMetricResult.Canceled)
                cancelableAction.cancel()
            }
        }.close()
    }
}