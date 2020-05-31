package app.gyst.ui.account.login

import androidx.navigation.NavDirections
import app.gyst.R
import app.gyst.biometrics.Biometrics
import app.gyst.common.asNavDirection
import app.gyst.common.exhaustive
import app.gyst.common.viewmodel.*
import app.gyst.persistence.model.hasProfile
import app.gyst.repository.AppPreferenceRepository
import app.gyst.repository.UserAccountRepository
import app.gyst.viewmodel.usecases.account.LoginAction
import app.gyst.viewmodel.usecases.account.LoginResults
import app.gyst.viewmodel.usecases.account.LoginUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

sealed class LoginScreenState : State {
    data class Initial(val withBiometrics:Boolean = false) : LoginScreenState()
    object LoggingIn : LoginScreenState()
    object InvalidCredentials : LoginScreenState()
    object ServiceFailure : LoginScreenState()
    data class Navigate(val direction: NavDirections) : LoginScreenState()
    object BiometricAuthFailure: LoginScreenState()
}

sealed class LoginScreenEvents : Event {
    data class OnLogin(val email: String, val password: String) : LoginScreenEvents()
    object OnLoginWithBioMetrics: LoginScreenEvents()
}

@FlowPreview
@ExperimentalCoroutinesApi
class LoginScreenViewModel(
    loginUseCase: LoginUseCase,
    private val biometrics: Biometrics,
    private val appPreferenceRepository: AppPreferenceRepository,
    private val userAccountRepository: UserAccountRepository
) : BaseViewModel<LoginScreenEvents, LoginScreenState>() {

    override val useCases: List<UseCase> = listOf(loginUseCase)

    override fun makeInitState(): LoginScreenState = LoginScreenState.Initial(
        withBiometrics = biometrics.canAuthenticate() && appPreferenceRepository.enableBiometrics
    )

    override fun Flow<LoginScreenEvents>.eventTransform(): Flow<Action> = flow {
        collect { event ->
            when (event) {
                is LoginScreenEvents.OnLogin -> emit(LoginAction.CredentialsAction(event.email, event.password))
                LoginScreenEvents.OnLoginWithBioMetrics -> emit(LoginAction.BioMetricAction)
            }.exhaustive
        }
    }

    override fun LoginScreenState.plus(result: Result): LoginScreenState = when (result) {
        is LoginResults -> {
            when (result) {
                LoginResults.LoggingIn -> LoginScreenState.LoggingIn
                LoginResults.LoginCompleted -> LoginScreenState.Navigate(nextNavigationTarget())
                LoginResults.InvalidCredentials -> LoginScreenState.InvalidCredentials
                LoginResults.ServiceFailure -> LoginScreenState.ServiceFailure
                LoginResults.BiometricAuthFailure -> LoginScreenState.BiometricAuthFailure
            }.exhaustive
        }
        else -> this
    }

    private fun nextNavigationTarget(): NavDirections =
        if (biometrics.canAuthenticate() && !appPreferenceRepository.hasCompletedBioMetricPrompt)
            R.id.nav_biometric_permission.asNavDirection()
        else if (userAccountRepository.userWithProfile?.hasProfile == true)  {
            R.id.nav_financial_overview.asNavDirection()
        } else {
            R.id.nav_introduction_screen.asNavDirection()
        }
}
