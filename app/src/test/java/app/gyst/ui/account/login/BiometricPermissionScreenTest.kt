package app.gyst.ui.account.login

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.R
import app.gyst.app.loadModules
import app.gyst.biometrics.Biometrics
import app.gyst.biometrics.model.BioMetricResult
import app.gyst.common.asNavDirection
import com.google.common.truth.Truth
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
class BiometricPermissionScreenTest {

    private fun createScenario(
        biometricPermissionScreenState: LiveData<BiometricPermissionScreenState> = mockk(relaxed = true),
        biometricPermissionScreenViewModel: BiometricPermissionScreenViewModel = mockk(relaxed = true) {
            every { state } returns biometricPermissionScreenState
        },
        biometrics: Biometrics = mockk(relaxed = true)
    ): FragmentScenario<BiometricPermissionScreen> {

        loadModules(module {
            viewModel { biometricPermissionScreenViewModel }
            single { biometrics }
        })

        return FragmentScenario.launchInContainer(
            BiometricPermissionScreen::class.java,
            null,
            R.style.GystTheme,
            null
        ).onFragment { fragment ->
            fragment.stateObserver.onChanged(BiometricPermissionScreenState.Initial)
        }
    }


    // Setup

    @Test
    fun observes_state_changes() {
        val state: LiveData<BiometricPermissionScreenState> = mockk(relaxed = true)
        createScenario(state).onFragment { fragment ->
            verify { state.observe(fragment.viewLifecycleOwner, fragment.stateObserver) }
        }
    }


    // State Renderings

    @Test
    fun renders__initial_state__skip_dispatches() {
        val viewModel: BiometricPermissionScreenViewModel = mockk(relaxed = true)
        createScenario(biometricPermissionScreenViewModel = viewModel).onFragment { fragment ->
            fragment.binder.skip.performClick()

            verify { viewModel.dispatchEvent(BiometricPermissionScreenEvents.OnSkip) }
        }
    }

    @Test
    fun renders__initial_state__enable_prompts_for_auth() {
        val biometrics: Biometrics = mockk(relaxed = true) {
            every { canAuthenticate() } returns true
        }
        val viewModel: BiometricPermissionScreenViewModel = mockk(relaxed = true)
        createScenario(biometricPermissionScreenViewModel = viewModel, biometrics = biometrics).onFragment { fragment ->
            fragment.binder.enable.performClick()

            verify { biometrics.promptForAuth(fragment.requireContext(), fragment.biometricObserver) }
        }
    }

    @Test
    fun renders__initial_state__biometric_success__enables_biometrics() {
        val biometrics: Biometrics = mockk(relaxed = true) {
            every { canAuthenticate() } returns true
        }
        val viewModel: BiometricPermissionScreenViewModel = mockk(relaxed = true)
        createScenario(biometricPermissionScreenViewModel = viewModel, biometrics = biometrics).onFragment { fragment ->
            fragment.biometricObserver.onBiometricResult(BioMetricResult.Success)

            verify { viewModel.dispatchEvent(BiometricPermissionScreenEvents.OnEnable) }
        }
    }

    @Test
    fun renders__navigation_state() {
        val navController: NavController = mockk(relaxed = true)

        createScenario().onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    // The fragment’s view has just been created
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }

            fragment.stateObserver.onChanged(BiometricPermissionScreenState.Navigate(R.id.nav_introduction_screen.asNavDirection()))

            val slotDirections = slot<NavDirections>()
            val slotOptions = slot<NavOptions>()
            verify { navController.navigate(capture(slotDirections), capture(slotOptions)) }

            Truth.assertThat(slotDirections.captured.actionId)
                .isEqualTo(R.id.nav_introduction_screen)
            Truth.assertThat(slotOptions.captured.popUpTo).isEqualTo(R.id.navigation_main)
            Truth.assertThat(slotOptions.captured.isPopUpToInclusive).isTrue()
        }
    }
}