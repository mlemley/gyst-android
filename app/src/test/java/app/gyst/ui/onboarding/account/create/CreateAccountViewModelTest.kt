package app.gyst.ui.onboarding.account.create

import app.gyst.R
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.repository.UserAccountRepository
import app.gyst.validation.PasswordRules
import app.gyst.validation.PasswordStrength
import app.gyst.viewmodel.usecases.account.CreateAccountActions
import app.gyst.viewmodel.usecases.account.CreateAccountResults
import app.gyst.viewmodel.usecases.account.CreateAccountUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class CreateAccountViewModelTest {

    private fun createViewModel(
        createAccountUseCase: CreateAccountUseCase = mockk(relaxed = true),
        passwordRules: PasswordRules = mockk(relaxUnitFun = true),
        userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true) {
            every { hasAccount } returns false
        }
    ): CreateAccountViewModel = CreateAccountViewModel(createAccountUseCase, userAccountRepository, passwordRules)

    @Test
    fun creates_initial_state() {
        val passwordRules = mockk<PasswordRules>(relaxed = true)
        assertThat(createViewModel(passwordRules = passwordRules).makeInitState()).isEqualTo(
            CreateAccountState.Initial(passwordRules)
        )
    }

    @Test
    fun instructs_to_navigate_to_next_when_account_already_created() {
        val userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true) {
            every { hasAccount } returns true
        }
        val initState = createViewModel(userAccountRepository = userAccountRepository).makeInitState()
        assertThat(initState).isInstanceOf(CreateAccountState.Navigate::class.java)
        assertThat((initState as CreateAccountState.Navigate).direction.actionId).isEqualTo(R.id.nav_login_screen)
    }

    @Test
    fun provides_list_of_use_cases() {
        val createAccountUseCase = mockk<CreateAccountUseCase>()
        assertThat(createViewModel(createAccountUseCase).useCases).isEqualTo(
            listOf(
                createAccountUseCase
            )
        )
    }

    @Test
    fun transforms_create_account_events__to__use_case_actions() {
        val email = "--email--"
        val password = "--password--"
        val events = flowOf(
            CreateAccountEvents.OnCreateAccount(email, password),
            CreateAccountEvents.OnPasswordStream("--password--")
        )

        val actualActions = mutableListOf<Action>()
        with(createViewModel()) {
            runBlocking {
                events.eventTransform().toList(actualActions)
            }
        }

        assertThat(actualActions).isEqualTo(
            listOf(
                CreateAccountActions.CreateAccount(email, password),
                CreateAccountActions.ValidatePasswordStrength(password)
            )
        )
    }

    @Test
    fun updates_state_with_results() {
        val viewModel = createViewModel()
        val initialState = viewModel.makeInitState()
        val missingStrengths = listOf<PasswordStrength>(PasswordStrength.Length)

        val results = listOf<Result>(
            CreateAccountResults.CreatingAccount,
            CreateAccountResults.InvalidEmail,
            CreateAccountResults.AccountCreated,
            CreateAccountResults.AccountExists,
            CreateAccountResults.PasswordValidationResults(missingStrengths)
        )

        val actualStates = mutableListOf<CreateAccountState>()
        with(viewModel) {
            results.forEach { actualStates.add(initialState + it) }
        }

        assertThat(actualStates).isEqualTo(
            listOf(
                CreateAccountState.Loading,
                CreateAccountState.InvalidEmail,
                CreateAccountState.Navigate(CreateAccountScreenDirections.actionNavOnboardingCreateAccountToNavIntroductionScreen()),
                CreateAccountState.AccountExists,
                CreateAccountState.PasswordEntry(missingStrengths)
            )
        )
    }
}