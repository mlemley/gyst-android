package app.gyst.ui.splash

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.app.loadModules
import app.gyst.app.verifyNavigation
import app.gyst.app.withMockedNavigation
import app.gyst.shadows.ShadowSnackbar
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.annotation.Config

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
        val navDirections = SplashScreenDirections.actionNavSplashScreenToNavOnboardingCreateAccount()
        createScenario().onFragment { fragment ->
            val navController: NavController = fragment.withMockedNavigation()
            fragment.stateObserver.onChanged(SplashScreenState.NavigationTask(navDirections))

            navController.verifyNavigation{ directions, options ->
                assertThat(directions.actionId).isEqualTo(navDirections.actionId)
                assertThat(options).isNull()
            }
        }
    }
}