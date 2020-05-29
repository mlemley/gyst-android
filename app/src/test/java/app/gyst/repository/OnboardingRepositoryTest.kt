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
class OnboardingRepositoryTest {

    private fun createSharedPreferences(): SharedPreferences =
        ApplicationProvider.getApplicationContext<TestGystApplication>().getSharedPreferences("SP", Context.MODE_PRIVATE)

    private fun createRepository(
        sharedPreferences: SharedPreferences
    ): OnboardingRepository = OnboardingRepository(sharedPreferences)

    @Test
    fun has_on_boarded__fetches_from_shared_preferences() {
        val sharedPreferences = createSharedPreferences()
        val repository = createRepository(sharedPreferences)

        assertThat(repository.hasOnBoarded).isFalse()

        sharedPreferences.edit {
            putBoolean(OnboardingRepository.PreferenceKey, true)
        }

        assertThat(repository.hasOnBoarded).isTrue()
    }

    @Test
    fun sets_onboarding_when_completed() {
        val sharedPreferences = createSharedPreferences()
        val repository = createRepository(sharedPreferences)

        repository.onBoardingComplete()

        assertThat(repository.hasOnBoarded).isTrue()
    }
}