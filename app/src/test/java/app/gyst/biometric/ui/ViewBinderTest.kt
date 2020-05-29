package app.gyst.biometric.ui

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.biometrics.BiometricObserver
import app.gyst.biometrics.cancelation.CancelableAction
import app.gyst.biometrics.cancelation.SignalProvider
import app.gyst.biometrics.ui.V23Dialog
import app.gyst.biometrics.ui.ViewBinder
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class ViewBinderTest {
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

    @Test
    fun views_can_be_bound() {
        createScenario().onActivity {
            val binder = ViewBinder(dialog().delegate)
            assertThat(binder.title).isNotNull()
            assertThat(binder.subTitle).isNotNull()
            assertThat(binder.description).isNotNull()
            assertThat(binder.stateIcon).isNotNull()
            assertThat(binder.helpMessage).isNotNull()
            assertThat(binder.positiveButton).isNotNull()
            assertThat(binder.negativeButton).isNotNull()
        }
    }
}