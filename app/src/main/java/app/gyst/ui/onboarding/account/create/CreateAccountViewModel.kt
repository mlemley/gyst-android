package app.gyst.ui.onboarding.account.create

import androidx.navigation.NavDirections
import app.gyst.R
import app.gyst.common.asNavDirection
import app.gyst.common.exhaustive
import app.gyst.common.viewmodel.*
import app.gyst.repository.UserAccountRepository
import app.gyst.validation.PasswordRules
import app.gyst.validation.PasswordStrength
import app.gyst.viewmodel.usecases.account.CreateAccountActions
import app.gyst.viewmodel.usecases.account.CreateAccountResults
import app.gyst.viewmodel.usecases.account.CreateAccountUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

sealed class CreateAccountState : State {
    data class Initial(val passwordRules: PasswordRules) : CreateAccountState()
    object Loading : CreateAccountState()
    object InvalidEmail : CreateAccountState()
    data class PasswordEntry(val missing: List<PasswordStrength>) : CreateAccountState()
    data class Navigate(val direction: NavDirections) : CreateAccountState()
    object AccountExists : CreateAccountState()
    object AccountCreationFailure : CreateAccountState()
}

sealed class CreateAccountEvents : Event {
    data class OnPasswordStream(val password: String) : CreateAccountEvents()
    data class OnCreateAccount(val email: String, val password: String) : CreateAccountEvents()
}

@FlowPreview
@ExperimentalCoroutinesApi
class CreateAccountViewModel(
    createAccountUseCase: CreateAccountUseCase,
    private val userAccountRepository: UserAccountRepository,
    private val passwordRules: PasswordRules
) : BaseViewModel<CreateAccountEvents, CreateAccountState>() {

    override val useCases: List<UseCase> = listOf(createAccountUseCase)

    override fun makeInitState(): CreateAccountState = if (userAccountRepository.hasAccount)
        CreateAccountState.Navigate(R.id.nav_login_screen.asNavDirection())
    else CreateAccountState.Initial(passwordRules)

    override fun Flow<CreateAccountEvents>.eventTransform(): Flow<Action> = flow {
        collect { event ->
            when (event) {
                is CreateAccountEvents.OnPasswordStream -> emit(
                    CreateAccountActions.ValidatePasswordStrength(
                        event.password
                    )
                )
                is CreateAccountEvents.OnCreateAccount -> emit(
                    CreateAccountActions.CreateAccount(
                        event.email,
                        event.password
                    )
                )
            }.exhaustive
        }
    }

    override fun CreateAccountState.plus(result: Result): CreateAccountState = when (result) {
        is CreateAccountResults -> {
            when (result) {
                CreateAccountResults.CreatingAccount -> CreateAccountState.Loading
                CreateAccountResults.AccountCreated -> CreateAccountState.Navigate(
                    CreateAccountScreenDirections.actionNavOnboardingCreateAccountToNavIntroductionScreen()
                )
                CreateAccountResults.InvalidEmail -> CreateAccountState.InvalidEmail
                CreateAccountResults.AccountExists -> CreateAccountState.AccountExists
                is CreateAccountResults.PasswordValidationResults -> CreateAccountState.PasswordEntry(result.missing)
                CreateAccountResults.CreationFailure -> CreateAccountState.AccountCreationFailure
            }.exhaustive
        }
        else -> this
    }

}
