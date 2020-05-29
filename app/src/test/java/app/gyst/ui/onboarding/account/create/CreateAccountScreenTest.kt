package app.gyst.ui.onboarding.account.create

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
import app.gyst.common.textValue
import app.gyst.shadows.ShadowSnackbar
import app.gyst.validation.PasswordRules
import app.gyst.validation.PasswordStrength
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.annotation.Config

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CreateAccountScreenTest {

    private fun createScenario(
        liveData: LiveData<CreateAccountState> = mockk(relaxUnitFun = true),
        createAccountViewModel: CreateAccountViewModel = mockk {
            every { state } returns liveData
        },
        passwordRules: PasswordRules = PasswordRules()
    ): FragmentScenario<CreateAccountScreen> {
        loadModules(module {
            viewModel { createAccountViewModel }
        })
        return FragmentScenario.launchInContainer(
            CreateAccountScreen::class.java,
            null,
            R.style.GystTheme,
            null
        ).onFragment {
            it.stateObserver.onChanged(CreateAccountState.Initial(passwordRules))
        }
    }

    @Test
    fun observes_state_changes() {
        val liveData: LiveData<CreateAccountState> = mockk(relaxUnitFun = true)
        createScenario(liveData).onFragment { fragment ->
            verify { liveData.observe(fragment.viewLifecycleOwner, fragment.stateObserver) }
        }
    }

    @Test
    fun streams_password_entry() {
        val viewModel: CreateAccountViewModel = mockk(relaxed = true)
        createScenario(createAccountViewModel = viewModel).onFragment { fragment ->

            fragment.binder.password.setText("1")
            fragment.binder.password.setText("1a")

            verify {
                viewModel.dispatchEvent(CreateAccountEvents.OnPasswordStream("1"))
                viewModel.dispatchEvent(CreateAccountEvents.OnPasswordStream("1a"))
            }
        }
    }

    @Test
    fun renders_initial_state() {
        val passwordRules = PasswordRules()
        createScenario(passwordRules = passwordRules).onFragment { fragment ->
            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isFalse()
                assertThat(rulesLower.isEnabled).isFalse()
                assertThat(rulesDigits.isEnabled).isFalse()
                assertThat(rulesSpecial.isEnabled).isFalse()
                assertThat(rulesSpecial.textValue).isEqualTo(passwordRules.specialChars)
                assertThat(rulesLength.isEnabled).isFalse()
                assertThat(rulesLength.textValue).isEqualTo("${passwordRules.minSize}-${passwordRules.maxSize}")
            }
        }
    }

    @Test
    fun creates_account__when_next_button_pressed() {
        val email = "--email--"
        val password = "--password--"
        val viewModel: CreateAccountViewModel = mockk(relaxed = true)
        createScenario(createAccountViewModel = viewModel).onFragment { fragment ->
            fragment.binder.emailAddress.setText(email)
            fragment.binder.password.setText(password)

            fragment.binder.next.performClick()

            verify {
                viewModel.dispatchEvent(CreateAccountEvents.OnCreateAccount(email, password))
            }
        }
    }

    @Test
    fun creates_account__when__password__ime_action_done() {
        val email = "--email--"
        val password = "--password--"
        val viewModel: CreateAccountViewModel = mockk(relaxed = true)
        createScenario(createAccountViewModel = viewModel).onFragment { fragment ->
            with(fragment.binder) {
                emailAddress.setText(email)
                this.password.setText(password)
            }

            fragment.binder.password.onEditorAction(EditorInfo.IME_ACTION_DONE)

            verify { viewModel.dispatchEvent(CreateAccountEvents.OnCreateAccount(email, password)) }
        }
    }

    @Test
    fun loading_clears_errors() {
        val email = "--email--"
        val password = "--password--"
        val viewModel: CreateAccountViewModel = mockk(relaxed = true)
        createScenario(createAccountViewModel = viewModel).onFragment { fragment ->
            fragment.binder.emailAddressLayout.error = "not valid"
            fragment.binder.emailAddress.setText(email)
            fragment.binder.password.setText(password)
            fragment.binder.passwordLayout.error = "not valid"

            fragment.stateObserver.onChanged(CreateAccountState.Loading)

            val emailError = fragment.binder.emailAddressLayout.error
            assertThat(emailError).isNull()
            val passwordError = fragment.binder.passwordLayout.error
            assertThat(passwordError).isNull()
        }
    }

    // Validation

    @Test
    fun invalid_email_updates_input_state() {
        val email = "--email--"
        val password = "--password--"
        val viewModel: CreateAccountViewModel = mockk(relaxed = true)
        createScenario(createAccountViewModel = viewModel).onFragment { fragment ->
            fragment.binder.emailAddress.setText(email)
            fragment.binder.password.setText(password)

            fragment.stateObserver.onChanged(CreateAccountState.InvalidEmail)

            assertThat(fragment.binder.password.text.toString()).isEqualTo("")
            assertThat(fragment.binder.passwordLayout.error).isNull()

            assertThat(fragment.binder.emailAddress.text.toString()).isEqualTo(email)

            assertThat(fragment.binder.emailAddressLayout.error.toString()).isEqualTo(
                fragment.getString(
                    R.string.invalid_email
                )
            )

        }
    }

    @Test
    fun updates_password_entry_state() {
        val viewModel: CreateAccountViewModel = mockk(relaxed = true)
        createScenario(createAccountViewModel = viewModel).onFragment { fragment ->
            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isFalse()
                assertThat(rulesLower.isEnabled).isFalse()
                assertThat(rulesDigits.isEnabled).isFalse()
                assertThat(rulesSpecial.isEnabled).isFalse()
                assertThat(rulesLength.isEnabled).isFalse()
            }

            fragment.stateObserver.onChanged(CreateAccountState.PasswordEntry(listOf(PasswordStrength.SpecialValues, PasswordStrength.UpperCased, PasswordStrength.LowerCased, PasswordStrength.Digits, PasswordStrength.Length)))

            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isFalse()
                assertThat(rulesLower.isEnabled).isFalse()
                assertThat(rulesDigits.isEnabled).isFalse()
                assertThat(rulesSpecial.isEnabled).isFalse()
                assertThat(rulesLength.isEnabled).isFalse()
            }

            fragment.stateObserver.onChanged(CreateAccountState.PasswordEntry(listOf(PasswordStrength.UpperCased, PasswordStrength.LowerCased, PasswordStrength.Digits, PasswordStrength.Length)))

            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isFalse()
                assertThat(rulesLower.isEnabled).isFalse()
                assertThat(rulesDigits.isEnabled).isFalse()
                assertThat(rulesSpecial.isEnabled).isTrue()
                assertThat(rulesLength.isEnabled).isFalse()
            }

            fragment.stateObserver.onChanged(CreateAccountState.PasswordEntry(listOf(PasswordStrength.LowerCased, PasswordStrength.Digits, PasswordStrength.Length)))

            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isTrue()
                assertThat(rulesLower.isEnabled).isFalse()
                assertThat(rulesDigits.isEnabled).isFalse()
                assertThat(rulesSpecial.isEnabled).isTrue()
                assertThat(rulesLength.isEnabled).isFalse()
            }

            fragment.stateObserver.onChanged(CreateAccountState.PasswordEntry(listOf(PasswordStrength.Digits, PasswordStrength.Length)))

            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isTrue()
                assertThat(rulesLower.isEnabled).isTrue()
                assertThat(rulesDigits.isEnabled).isFalse()
                assertThat(rulesSpecial.isEnabled).isTrue()
                assertThat(rulesLength.isEnabled).isFalse()
            }

            fragment.stateObserver.onChanged(CreateAccountState.PasswordEntry(listOf(PasswordStrength.Length)))

            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isTrue()
                assertThat(rulesLower.isEnabled).isTrue()
                assertThat(rulesDigits.isEnabled).isTrue()
                assertThat(rulesSpecial.isEnabled).isTrue()
                assertThat(rulesLength.isEnabled).isFalse()
            }

            fragment.stateObserver.onChanged(CreateAccountState.PasswordEntry(listOf()))

            with(fragment.binder) {
                assertThat(rulesUpper.isEnabled).isTrue()
                assertThat(rulesLower.isEnabled).isTrue()
                assertThat(rulesDigits.isEnabled).isTrue()
                assertThat(rulesSpecial.isEnabled).isTrue()
                assertThat(rulesLength.isEnabled).isTrue()
            }
        }
    }

    // Error Handling

    @Test
    @Config(shadows = [ShadowSnackbar::class])
    fun state_observer__request_failed__shows_snack_bar() {
        createScenario().onFragment { fragment ->
            fragment.stateObserver.onChanged(CreateAccountState.AccountCreationFailure)

            assertThat(ShadowSnackbar.getTextOfLatestSnackbar()).isEqualTo(fragment.getString(R.string.account_creation_failure_message))
        }
    }

    @Test
    @Config(shadows = [ShadowSnackbar::class])
    fun state_observer__account_exists_failure__shows_snack_bar() {
        createScenario().onFragment { fragment ->
            fragment.stateObserver.onChanged(CreateAccountState.AccountExists)

            assertThat(ShadowSnackbar.getTextOfLatestSnackbar()).isEqualTo(fragment.getString(R.string.account_creation_conflict_failure_message))
        }
    }

    @Test
    fun navigates_to_next_destination() {
        val navController: NavController = mockk(relaxed = true)

        createScenario().onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    // The fragment’s view has just been created
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }

            fragment.stateObserver.onChanged(CreateAccountState.Navigate(CreateAccountScreenDirections.actionNavOnboardingCreateAccountToNavIntroductionScreen()))

            val slotDirections = slot<NavDirections>()
            val slotOptions = slot<NavOptions>()
            verify { navController.navigate(capture(slotDirections), capture(slotOptions)) }

            assertThat(slotDirections.captured.actionId).isEqualTo(R.id.action_nav_onboarding_create_account_to_nav_introduction_screen)
            assertThat(slotOptions.captured.popUpTo).isEqualTo(R.id.navigation_main)
            assertThat(slotOptions.captured.isPopUpToInclusive).isTrue()
        }
    }

    @Ignore
    @Test
    fun clicking_login__navigates__to_login_screen() {
        val navController: NavController = mockk(relaxed = true)

        createScenario().onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    // The fragment’s view has just been created
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
            fragment.binder.login.performClick()

            val slotDirections = slot<NavDirections>()
            verify { navController.navigate(capture(slotDirections)) }

            assertThat(slotDirections.captured.actionId).isEqualTo(R.id.nav_login_screen)
        }
    }
}