package app.gyst.viewmodel.usecases.account

import app.gyst.client.GystClient
import app.gyst.client.SafeResponse
import app.gyst.client.SessionManager
import app.gyst.client.model.CreateUserRequest
import app.gyst.client.safeCall
import app.gyst.common.exhaustive
import app.gyst.common.isValidEmail
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.UseCase
import app.gyst.repository.UserAccountRepository
import app.gyst.validation.PasswordRules
import app.gyst.validation.PasswordStrength
import app.gyst.validation.isValidPassword
import app.gyst.validation.passwordWeaknesses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking


sealed class CreateAccountActions : Action {
    data class ValidatePasswordStrength(val password: String) : CreateAccountActions()
    data class CreateAccount(val email: String, val password: String) : CreateAccountActions()
}

sealed class CreateAccountResults : Result {
    object CreatingAccount : CreateAccountResults()
    object AccountCreated : CreateAccountResults()
    object InvalidEmail : CreateAccountResults()
    object AccountExists : CreateAccountResults()
    object CreationFailure : CreateAccountResults()
    data class PasswordValidationResults(val missing: List<PasswordStrength>) :
        CreateAccountResults()
}

@ExperimentalCoroutinesApi
class CreateAccountUseCase(
    private val userAccountRepository: UserAccountRepository,
    private val gystClient: GystClient,
    private val passwordRules: PasswordRules,
    private val sessionManager: SessionManager
) : UseCase {
    override fun canProcess(action: Action): Boolean = action is CreateAccountActions

    override fun handleAction(action: Action): Flow<Result> = when (action) {
        is CreateAccountActions -> {
            when (action) {
                is CreateAccountActions.ValidatePasswordStrength -> flowOf(
                    CreateAccountResults.PasswordValidationResults(
                        action.password.passwordWeaknesses(
                            passwordRules
                        )
                    )
                )
                is CreateAccountActions.CreateAccount -> handleCreateAccount(
                    action.email,
                    action.password
                )
            }.exhaustive
        }
        else -> emptyFlow()
    }

    private fun handleCreateAccount(email: String, password: String): Flow<Result> =
        channelFlow<Result> {
            send(CreateAccountResults.CreatingAccount)
            if (!email.isValidEmail()) {
                send(CreateAccountResults.InvalidEmail)
                return@channelFlow
            }
            if (!password.isValidPassword(passwordRules)) {
                send(CreateAccountResults.PasswordValidationResults(password.passwordWeaknesses(passwordRules)))
                return@channelFlow
            }


            safeCall { runBlocking { gystClient.createUserAccount(CreateUserRequest(email, password)) } }.let { response ->
                when (response) {
                    is SafeResponse.Success -> {
                        response.data?.let { loginResponse ->
                            sessionManager.authenticatedWith(loginResponse.accessToken)
                            userAccountRepository.cacheAccount(loginResponse)
                            send(CreateAccountResults.AccountCreated)
                        } ?: send(CreateAccountResults.CreationFailure)
                    }
                    is SafeResponse.Error.Conflict -> send(CreateAccountResults.AccountExists)
                    is SafeResponse.Error -> send(CreateAccountResults.CreationFailure)
                }.exhaustive
            }
        }.flowOn(Dispatchers.IO)

}
