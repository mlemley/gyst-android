package app.gyst.ui.account.login

import app.gyst.R
import app.gyst.biometrics.Biometrics
import app.gyst.common.asNavDirection
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.common.viewmodel.State
import app.gyst.repository.AppPreferenceRepository
import app.gyst.viewmodel.usecases.account.LoginAction
import app.gyst.viewmodel.usecases.account.LoginResults
import app.gyst.viewmodel.usecases.account.LoginUseCase
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
class LoginScreenViewModelTest {

    private fun createViewModel(
        loginUseCase: LoginUseCase = mockk(relaxUnitFun = true),
        biometrics: Biometrics = mockk(relaxed = true),
        appPreferenceRepository: AppPreferenceRepository = mockk(relaxed = true)
    ): LoginScreenViewModel = LoginScreenViewModel(loginUseCase, biometrics, appPreferenceRepository)

    @Test
    fun provides_list_of_use_cases() {
        val loginUseCase: LoginUseCase = mockk(relaxUnitFun = true)
        assertThat(createViewModel(loginUseCase).useCases).isEqualTo(listOf(loginUseCase))
    }

    @Test
    fun creates_initial_state() {
        val biometrics: Biometrics = mockk(relaxed = true) {
            every { canAuthenticate() } returns false andThen true
        }
        val appPreferenceRepository: AppPreferenceRepository = mockk(relaxed = true) {
            every { enableBiometrics } returns false andThen true
        }
        val viewModel = createViewModel(biometrics = biometrics, appPreferenceRepository = appPreferenceRepository)
        assertThat(viewModel.makeInitState()).isEqualTo(LoginScreenState.Initial(false))
        assertThat(viewModel.makeInitState()).isEqualTo(LoginScreenState.Initial(false))
        assertThat(viewModel.makeInitState()).isEqualTo(LoginScreenState.Initial(true))
    }


    @Test
    fun maps__view_model_events__to__use_case_actions() = runBlocking {
        val email = "--email--"
        val password = "--password--"
        val viewModel = createViewModel()
        val events = flowOf(
            LoginScreenEvents.OnLogin(email, password),
            LoginScreenEvents.OnLoginWithBioMetrics
        )

        val actualActions = mutableListOf<Action>()
        with(viewModel) {
            events.eventTransform().toList(actualActions)
        }

        assertThat(actualActions).isEqualTo(
            listOf(
                LoginAction.CredentialsAction(email, password),
                LoginAction.BioMetricAction
            )
        )
    }

    @Test
    fun creates_state_from_results() {
        val biometrics: Biometrics = mockk {
            every { canAuthenticate() } returns false andThen false andThen true
        }

        val appPreferenceRepository: AppPreferenceRepository = mockk {
            every { hasCompletedBioMetricPrompt } returns false andThen true
        }
        val viewModel = createViewModel(biometrics = biometrics, appPreferenceRepository = appPreferenceRepository)
        val initialState = viewModel.makeInitState()
        val results = listOf<Result>(
            LoginResults.LoggingIn,
            LoginResults.LoginCompleted,
            LoginResults.LoginCompleted,
            LoginResults.LoginCompleted,
            LoginResults.InvalidCredentials,
            LoginResults.ServiceFailure,
            LoginResults.BiometricAuthFailure
        )

        val actualStates = mutableListOf<State>()
        with(viewModel) {
            results.forEach {
                actualStates.add(initialState + it)
            }
        }

        assertThat(actualStates).isEqualTo(
            listOf(
                LoginScreenState.LoggingIn,
                LoginScreenState.Navigate(R.id.nav_introduction_screen.asNavDirection()),
                LoginScreenState.Navigate(R.id.nav_biometric_permission.asNavDirection()),
                LoginScreenState.Navigate(R.id.nav_introduction_screen.asNavDirection()),
                LoginScreenState.InvalidCredentials,
                LoginScreenState.ServiceFailure,
                LoginScreenState.BiometricAuthFailure
            )
        )
    }

}