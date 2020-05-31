package app.gyst.ui.account.login

import app.gyst.R
import app.gyst.common.asNavDirection
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.State
import app.gyst.repository.UserAccountRepository
import app.gyst.viewmodel.usecases.BiometricPermissionActions
import app.gyst.viewmodel.usecases.BiometricPermissionCollectedResult
import app.gyst.viewmodel.usecases.BiometricPermissionUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class BiometricPermissionScreenViewModelTest {

    private fun createViewModel(
        biometricPermissionUseCase: BiometricPermissionUseCase = mockk(relaxed = true),
        userAccountRepository: UserAccountRepository = mockk(relaxed = true)
    ): BiometricPermissionScreenViewModel = BiometricPermissionScreenViewModel(biometricPermissionUseCase, userAccountRepository)

    @Test
    fun provides_list_of_used_use_cases() {
        val biometricPermissionUseCase: BiometricPermissionUseCase = mockk()
        assertThat(createViewModel(biometricPermissionUseCase).useCases).isEqualTo(listOf(biometricPermissionUseCase))
    }

    @Test
    fun makes_initial_state() {
        assertThat(createViewModel().makeInitState()).isEqualTo(BiometricPermissionScreenState.Initial)
    }

    @Test
    fun maps__view_model_events__to__use_case_actions() = runBlocking {
        val viewModel = createViewModel()

        val events = flowOf(
            BiometricPermissionScreenEvents.OnEnable,
            BiometricPermissionScreenEvents.OnSkip
        )

        val actualActions = mutableListOf<Action>()

        with(viewModel) {
            events.eventTransform().toList(actualActions)
        }

        assertThat(actualActions).isEqualTo(
            listOf(
                BiometricPermissionActions.Enable,
                BiometricPermissionActions.Disable
            )
        )
    }

    @Test
    fun updates_state() = runBlocking {
        val userAccountRepository: UserAccountRepository = mockk {
            every { userWithProfile } returns mockk {
                every { profileId } returns null andThen UUID.randomUUID()
            }
        }
        val viewModel = createViewModel(userAccountRepository = userAccountRepository)
        val initialState = viewModel.makeInitState()

        val results = listOf<Result>(
            BiometricPermissionCollectedResult(true),
            BiometricPermissionCollectedResult(true)
        )

        val states = mutableListOf<State>()
        with(viewModel) {
            results.forEach {
                states.add(initialState + it)
            }
        }

        assertThat(states).isEqualTo(
            listOf(
                BiometricPermissionScreenState.Navigate(R.id.nav_introduction_screen.asNavDirection()),
                BiometricPermissionScreenState.Navigate(R.id.nav_financial_overview.asNavDirection())
            )
        )
    }

}
