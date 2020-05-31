package app.gyst.ui.onboarding.account.profile

import android.view.inputmethod.EditorInfo
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.R
import app.gyst.app.loadModules
import app.gyst.app.verifyNavigation
import app.gyst.app.withMockedNavigation
import app.gyst.common.asNavDirection
import app.gyst.shadows.ShadowSnackbar
import app.gyst.ui.onboarding.account.profile.CreateProfileValidationErrors.FirstNameEmpty
import app.gyst.ui.onboarding.account.profile.CreateProfileValidationErrors.LastNameEmpty
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
class CreateProfileScreenTest {

    private fun createScenario(
        liveData: LiveData<CreateProfileState> = mockk(relaxUnitFun = true),
        createProfileViewModel: CreateProfileViewModel = mockk(relaxUnitFun = true) {
            every { state } returns liveData
        }
    ): FragmentScenario<CreateProfileScreen> {
        loadModules(
            module {
                viewModel { createProfileViewModel }
            }
        )

        return FragmentScenario.launchInContainer(
            CreateProfileScreen::class.java,
            null,
            R.style.GystTheme,
            null
        ).onFragment { fragment ->
            fragment.stateObserver.onChanged(CreateProfileState.Initial)
        }
    }

    // View Setup

    @Test
    fun observes_state_changes() {
        val state: LiveData<CreateProfileState> = mockk(relaxUnitFun = true)

        createScenario(state).onFragment { fragment ->
            verify { state.observe(fragment.viewLifecycleOwner, fragment.stateObserver) }
        }
    }

    // State Rendering

    @Test
    fun rendersInitialState__next_button_dispatches_process_event() {
        val firstName = "--first-name--"
        val lastName = "--last-name--"
        val viewModel: CreateProfileViewModel = mockk(relaxed = true)
        createScenario(createProfileViewModel = viewModel).onFragment { fragment ->
            fragment.binder.firstName.setText(firstName)
            fragment.binder.lastName.setText(lastName)

            fragment.binder.next.performClick()

            verify {
                viewModel.dispatchEvent(CreateProfileEvents.OnProcessNext(firstName, lastName))
            }
        }
    }

    @Test
    fun rendersInitialState__last_name_ime_done_action__processes_event() {
        val firstName = "--first-name--"
        val lastName = "--last-name--"
        val viewModel: CreateProfileViewModel = mockk(relaxed = true)
        createScenario(createProfileViewModel = viewModel).onFragment { fragment ->
            fragment.binder.firstName.setText(firstName)
            fragment.binder.lastName.setText(lastName)

            fragment.binder.lastName.onEditorAction(EditorInfo.IME_ACTION_DONE)

            verify {
                viewModel.dispatchEvent(CreateProfileEvents.OnProcessNext(firstName, lastName))
            }
        }
    }

    @Test
    fun rendersInitialState__next_button__clears_errors() {
        val viewModel: CreateProfileViewModel = mockk(relaxed = true)
        createScenario(createProfileViewModel = viewModel).onFragment { fragment ->
            fragment.binder.firstNameLayout.error = "--error-message--"
            fragment.binder.lastNameLayout.error = "--error-message--"

            fragment.binder.next.performClick()

            val firstNameError = fragment.binder.firstNameLayout.error
            val lastNameError = fragment.binder.lastNameLayout.error
            assertThat(firstNameError).isNull()
            assertThat(lastNameError).isNull()
        }
    }


    @Test
    fun rendersInitialState__last_name_ime_done_action__clears_errors() {
        val viewModel: CreateProfileViewModel = mockk(relaxed = true)
        createScenario(createProfileViewModel = viewModel).onFragment { fragment ->
            fragment.binder.firstNameLayout.error = "--error-message--"
            fragment.binder.lastNameLayout.error = "--error-message--"

            fragment.binder.lastName.onEditorAction(EditorInfo.IME_ACTION_DONE)

            val firstNameError = fragment.binder.firstNameLayout.error
            val lastNameError = fragment.binder.lastNameLayout.error
            assertThat(firstNameError).isNull()
            assertThat(lastNameError).isNull()
        }
    }

    @Test
    fun renderInvalidInput__sets_errors() {
        val viewModel: CreateProfileViewModel = mockk(relaxed = true)
        createScenario(createProfileViewModel = viewModel).onFragment { fragment ->
            fragment.binder.firstNameLayout.error = null
            fragment.binder.lastNameLayout.error = null

            fragment.stateObserver.onChanged(CreateProfileState.InvalidInput(listOf(FirstNameEmpty, LastNameEmpty)))

            val firstNameError = fragment.binder.firstNameLayout.error
            val lastNameError = fragment.binder.lastNameLayout.error
            assertThat(firstNameError).isEqualTo(fragment.getString(R.string.first_name_empty_error))
            assertThat(lastNameError).isEqualTo(fragment.getString(R.string.last_name_empty_error))
        }
    }

    @Test
    fun renderNext__navigates_with_directions__popping_stack__inclusively() {
        val navDirections = R.id.nav_financial_overview.asNavDirection()
        val viewModel: CreateProfileViewModel = mockk(relaxed = true)

        createScenario(createProfileViewModel = viewModel).onFragment { fragment ->
            val navController = fragment.withMockedNavigation()
            fragment.stateObserver.onChanged(CreateProfileState.NavigationTask(navDirections))

            navController.verifyNavigation { directions, options ->
                assertThat(directions.actionId).isEqualTo(navDirections.actionId)
                assertThat(options.popUpTo).isEqualTo(R.id.navigation_main)
                assertThat(options.isPopUpToInclusive).isTrue()
            }
        }
    }

    @Test
    @Config(shadows = [ShadowSnackbar::class])
    fun renders_failure() {
        createScenario().onFragment { fragment ->
            fragment.stateObserver.onChanged(CreateProfileState.CreateProfileFailure)

            assertThat(ShadowSnackbar.getTextOfLatestSnackbar()).isEqualTo(fragment.getString(R.string.account_profile_creation_failure_message))
        }
    }
}

