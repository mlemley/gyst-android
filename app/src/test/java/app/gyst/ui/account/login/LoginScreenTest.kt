package app.gyst.ui.account.login

import android.view.inputmethod.EditorInfo
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.R
import app.gyst.app.loadModules
import app.gyst.common.asNavDirection
import app.gyst.common.textValue
import app.gyst.shadows.ShadowSnackbar
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
import org.robolectric.annotation.Config

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    private fun createScenario (
        stateLiveData:LiveData<LoginScreenState> = mockk(relaxed = true),
        loginScreenViewModel: LoginScreenViewModel = mockk(relaxed = true) {
            every { state } returns stateLiveData
        }
    ): FragmentScenario<LoginScreen> {
        loadModules(module {
            viewModel { loginScreenViewModel }
        })
        return FragmentScenario.launchInContainer(
            LoginScreen::class.java,
            null,
            R.style.GystTheme,
            null
        ).onFragment { fragment ->
            fragment.stateObserver.onChanged(LoginScreenState.Initial())
        }
    }

    // Setup
    @Test
    fun observes_state_changes() {
        val stateLiveData:LiveData<LoginScreenState> = mockk(relaxUnitFun = true)
        createScenario(stateLiveData).onFragment { fragment ->
            verify { stateLiveData.observe(fragment.viewLifecycleOwner, fragment.stateObserver) }
        }
    }

    // State Management

    @Test
    fun rendersInitialState__login_button_click__dispatches_on_login_event() {
        val viewModel:LoginScreenViewModel = mockk(relaxed = true)
        val email = "--email--"
        val password = "--password--"

        createScenario(loginScreenViewModel = viewModel).onFragment { fragment ->
            fragment.binder.emailAddress.setText(email)
            fragment.binder.password.setText(password)

            fragment.binder.login.performClick()

            verify { viewModel.dispatchEvent(LoginScreenEvents.OnLogin(email, password)) }
        }
    }

    @Test
    fun rendersInitialState__login_button_click__password_ime_action_submits() {
        val viewModel:LoginScreenViewModel = mockk(relaxed = true)
        val email = "--email--"
        val password = "--password--"

        createScenario(loginScreenViewModel = viewModel).onFragment { fragment ->
            fragment.binder.emailAddress.setText(email)
            fragment.binder.password.setText(password)

            fragment.binder.password.onEditorAction(EditorInfo.IME_ACTION_DONE)

            verify { viewModel.dispatchEvent(LoginScreenEvents.OnLogin(email, password)) }
        }
    }

    @Test
    fun renders_state__loading__clears_errors() {
        val viewModel:LoginScreenViewModel = mockk(relaxed = true)

        createScenario(loginScreenViewModel = viewModel).onFragment { fragment ->
            fragment.binder.emailAddressLayout.error = "--error-1--"
            fragment.binder.passwordLayout.error= "--error-2--"

            fragment.stateObserver.onChanged(LoginScreenState.LoggingIn)

            val emailError = fragment.binder.emailAddressLayout.error
            val passwordError = fragment.binder.passwordLayout.error
            assertThat(emailError).isNull()
            assertThat(passwordError).isNull()
        }
    }

    @Test
    fun renders_state__invalid_credentials() {
        val viewModel:LoginScreenViewModel = mockk(relaxed = true)

        createScenario(loginScreenViewModel = viewModel).onFragment { fragment ->
            fragment.stateObserver.onChanged(LoginScreenState.InvalidCredentials)

            val emailError = fragment.binder.emailAddressLayout.error
            assertThat(emailError).isEqualTo(fragment.getString(R.string.account_login_invalid_credentials_message))
            assertThat(fragment.binder.password.textValue).isEqualTo("")
        }
    }

    @Test
    fun render_navigation_state__pops_up_to_main() {
        val navController: NavController = mockk(relaxed = true)

        createScenario().onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    // The fragment’s view has just been created
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }

            fragment.stateObserver.onChanged(LoginScreenState.Navigate(R.id.nav_introduction_screen.asNavDirection()))

            val slotDirections = slot<NavDirections>()
            val slotOptions = slot<NavOptions>()
            verify { navController.navigate(capture(slotDirections), capture(slotOptions)) }

            assertThat(slotDirections.captured.actionId).isEqualTo(R.id.nav_introduction_screen)
            assertThat(slotOptions.captured.popUpTo).isEqualTo(R.id.navigation_main)
            assertThat(slotOptions.captured.isPopUpToInclusive).isTrue()
        }
    }

    @Test
    @Config(shadows = [ShadowSnackbar::class])
    fun renders_biometric_auth_failure() {
        createScenario().onFragment { fragment ->
            fragment.stateObserver.onChanged(LoginScreenState.BiometricAuthFailure)

            assertThat(ShadowSnackbar.getTextOfLatestSnackbar()).isEqualTo(fragment.getString(R.string.account_login_biometric_failure_message))
        }
    }
}