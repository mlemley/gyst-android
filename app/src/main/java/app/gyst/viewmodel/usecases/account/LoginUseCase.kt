package app.gyst.viewmodel.usecases.account

import app.gyst.client.GystClient
import app.gyst.client.SafeResponse
import app.gyst.client.SessionManager
import app.gyst.client.model.LoginRequest
import app.gyst.client.safeCall
import app.gyst.common.exhaustive
import app.gyst.common.isValidEmail
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.UseCase
import app.gyst.repository.UserAccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking

sealed class LoginAction : Action {
    data class CredentialsAction(val email: String, val password: String) : LoginAction()
    object BioMetricAction : LoginAction()
}

sealed class LoginResults : Result {
    object LoggingIn : LoginResults()
    object LoginCompleted : LoginResults()
    object InvalidCredentials : LoginResults()
    object ServiceFailure : LoginResults()
    object BiometricAuthFailure:LoginResults()
}

@ExperimentalCoroutinesApi
class LoginUseCase(
    private val userAccountRepository: UserAccountRepository,
    private val gystClient: GystClient,
    private val sessionManager: SessionManager
) : UseCase {
    override fun canProcess(action: Action): Boolean = action is LoginAction

    override fun handleAction(action: Action): Flow<Result> = channelFlow<Result> {
        when (action) {
            is LoginAction.CredentialsAction -> handleLoginWithCredentials(action.email, action.password)
            is LoginAction.BioMetricAction -> handleLoginWithBiometrics()
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun ProducerScope<Result>.handleLoginWithBiometrics() {
        userAccountRepository.userAccount?.let {user ->
            sessionManager.authenticatedWith(user.accessToken)
            userAccountRepository.updateLastSeen(user)
            send(LoginResults.LoginCompleted)
        } ?: send(LoginResults.BiometricAuthFailure)
    }

    private suspend fun ProducerScope<Result>.handleLoginWithCredentials(email: String, password: String) {
        send(LoginResults.LoggingIn)
        if (email.isValidEmail()) {
            safeCall { runBlocking { gystClient.login(LoginRequest(email, password)) } }.let { safeResponse ->
                when (safeResponse) {
                    is SafeResponse.Success -> {
                        safeResponse.data?.let { loginResponse ->
                            sessionManager.authenticatedWith(loginResponse.accessToken)
                            userAccountRepository.cacheAccount(loginResponse)
                            send(LoginResults.LoginCompleted)
                        } ?: send(LoginResults.ServiceFailure)
                    }
                    is SafeResponse.Error.NotAuthorized -> send(LoginResults.InvalidCredentials)
                    is SafeResponse.Error -> send(LoginResults.ServiceFailure)
                }.exhaustive
            }
        } else {
            send(LoginResults.InvalidCredentials)
        }
    }

}
