package app.gyst.viewmodel.usecases.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.app.TestCoroutineRule
import app.gyst.client.GystClient
import app.gyst.client.SessionManager
import app.gyst.client.model.LoginRequest
import app.gyst.client.model.LoginResponse
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.persistence.model.User
import app.gyst.repository.UserAccountRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant
import retrofit2.HttpException
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LoginUseCaseTest {

    @get:Rule
    val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createUseCase(
        userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true),
        gystClient: GystClient = mockk(relaxUnitFun = true),
        sessionManager: SessionManager = mockk(relaxUnitFun = true)
    ): LoginUseCase = LoginUseCase(userAccountRepository, gystClient, sessionManager)

    @Test
    fun handles_LoginAction() {
        val useCase = createUseCase()
        assertThat(useCase.canProcess(LoginAction.CredentialsAction("", ""))).isTrue()
        assertThat(useCase.canProcess(LoginAction.BioMetricAction)).isTrue()
        assertThat(useCase.canProcess(object : Action {})).isFalse()
    }

    @Test
    fun user_can_login__validates_email() = runBlocking {
        val email = "foo@bar"
        val password = "--password--"
        val useCase = createUseCase()

        val actualResults = mutableListOf<Result>()

        useCase.handleAction(LoginAction.CredentialsAction(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                LoginResults.LoggingIn,
                LoginResults.InvalidCredentials
            )
        )
    }

    @Test
    fun handling_action__performs_login__saves_access_token__saves__caches_user_data() = runBlocking {
        val email = "foo@bar.com"
        val password = "--password--"
        val accessToken = "--access-token--"
        val loginResponse = LoginResponse(
            UUID.randomUUID(),
            email,
            true,
            Instant.now(),
            Instant.now(),
            accessToken
        )
        val client: GystClient = mockk {
            every { runBlocking { login(LoginRequest(email, password)) } } returns loginResponse
        }

        val sessionManager: SessionManager = mockk(relaxed = true)
        val userAccountRepository: UserAccountRepository = mockk(relaxed = true)
        val useCase = createUseCase(userAccountRepository, client, sessionManager)

        val actualResults = mutableListOf<Result>()

        useCase.handleAction(LoginAction.CredentialsAction(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                LoginResults.LoggingIn,
                LoginResults.LoginCompleted
            )
        )

        verify {
            sessionManager.authenticatedWith(accessToken)
            runBlocking { userAccountRepository.cacheAccount(loginResponse) }
        }
    }

    @Test
    fun handling_action__communicates_failure() = runBlocking {
        val email = "foo@bar.com"
        val password = "--password--"
        val client: GystClient = mockk {
            every { runBlocking { login(LoginRequest(email, password)) } } throws mockk<HttpException>(relaxed = true) {
                every { code() } returns 500
                every { message } returns null
            }
        }

        val useCase = createUseCase(gystClient = client)
        val actualResults = mutableListOf<Result>()

        useCase.handleAction(LoginAction.CredentialsAction(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                LoginResults.LoggingIn,
                LoginResults.ServiceFailure
            )
        )
    }

    @Test
    fun handling_action__communicates_invalid_credentials() = runBlocking {
        val email = "foo@bar.com"
        val password = "--password--"
        val client: GystClient = mockk {
            every { runBlocking { login(any()) } } throws mockk<HttpException>(relaxed = true) {
                every { code() } returns 401
                every { message } returns null
            }
        }

        val useCase = createUseCase(gystClient = client)
        val actualResults = mutableListOf<Result>()

        useCase.handleAction(LoginAction.CredentialsAction(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                LoginResults.LoggingIn,
                LoginResults.InvalidCredentials
            )
        )
    }

    @Test
    fun logging_in_with_bio_metrics__adds_access_token_to_session() = runBlocking {
        val sessionManager: SessionManager = mockk(relaxed = true)
        val accessToken = "--token--"
        val user = User(UUID.randomUUID(), "", true, accessToken, Instant.now(), Instant.now(), Instant.now())
        val userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true) {
            every { userAccount } returns user
        }

        val useCase = createUseCase(userAccountRepository = userAccountRepository, sessionManager = sessionManager)

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(LoginAction.BioMetricAction).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                LoginResults.LoginCompleted
            )
        )

        verify { sessionManager.authenticatedWith(accessToken) }
        verify { runBlocking { userAccountRepository.updateLastSeen(user) }}
    }

    @Test
    fun logging_in_with_bio_metrics__communicates_failure__for_normal_login() = runBlocking {
        val userAccountRepository: UserAccountRepository = mockk {
            every { userAccount } returns null
        }
        val useCase = createUseCase(userAccountRepository = userAccountRepository)

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(LoginAction.BioMetricAction).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                LoginResults.BiometricAuthFailure
            )
        )

    }
}