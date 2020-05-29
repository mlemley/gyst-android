package app.gyst.ui.splash

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.app.loadModules
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    private fun createScenario(
        viewModelState: LiveData<SplashScreenState> = mockk(relaxed = true),
        splashScreenViewModel: SplashScreenViewModel = mockk {
            every { state } returns viewModelState
        }
    ): FragmentScenario<SplashScreen> {
        loadModules(module {
            viewModel { splashScreenViewModel }
        })
        return FragmentScenario.launchInContainer(SplashScreen::class.java)
    }

    @Test
    fun observes_state_changes() {
        val viewModelState: LiveData<SplashScreenState> = mockk(relaxed = true)

        createScenario(viewModelState = viewModelState).onFragment { fragment ->
            verify {
                viewModelState.observe(fragment.viewLifecycleOwner, fragment.stateObserver)
            }
        }

    }

    @Test
    fun navigates_to_destination_when_instructed() {
        val navController: NavController = mockk(relaxed = true)
        val navDirections = SplashScreenDirections.actionNavSplashScreenToNavOnboardingCreateAccount()
        createScenario().onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    // The fragment’s view has just been created
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }

            fragment.stateObserver.onChanged(SplashScreenState.NavigationTask(navDirections))

            val slot = slot<NavDirections>()
            verify { navController.navigate(capture(slot)) }

            assertThat(slot.captured.actionId).isEqualTo(navDirections.actionId)
        }
    }
}