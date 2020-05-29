package app.gyst.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.app.TestGystApplication
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

import org.junit.Test
import org.junit.runner.RunWith

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)


class AppPreferenceRepositoryTest {

    private fun sharedPreferences(): SharedPreferences =
        ApplicationProvider.getApplicationContext<TestGystApplication>()
            .getSharedPreferences("Prefs", Context.MODE_PRIVATE)

    private fun createRepository(): AppPreferenceRepository = AppPreferenceRepository(sharedPreferences())

    @Test
    fun use_bio_metric_auth() {
        val sharedPreferences = sharedPreferences()
        val repository = createRepository()

        assertThat(repository.hasCompletedBioMetricPrompt).isFalse()

        sharedPreferences.edit { putBoolean(BiometricPromptKey, true) }
        assertThat(repository.hasCompletedBioMetricPrompt).isTrue()
    }

    @Test
    fun has_biometric_auth_enabled() {
        val repository = createRepository()

        assertThat(repository.enableBiometrics).isFalse()

        repository.enableBiometrics = false
        assertThat(repository.enableBiometrics).isFalse()

        repository.enableBiometrics = true
        assertThat(repository.enableBiometrics).isTrue()
    }
}