package app.gyst.viewmodel.usecases.account

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.client.GystClient
import app.gyst.client.SessionManager
import app.gyst.client.model.CreateUserRequest
import app.gyst.client.model.LoginResponse
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.repository.UserAccountRepository
import app.gyst.validation.PasswordRules
import app.gyst.validation.PasswordStrength
import app.gyst.validation.isValidPassword
import app.gyst.validation.passwordWeaknesses
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant
import retrofit2.HttpException
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CreateAccountUseCaseTest {

    private fun createUseCase(
        userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true),
        gystClient: GystClient = mockk(relaxUnitFun = true),
        sessionManager: SessionManager = mockk(relaxUnitFun = true)
    ): CreateAccountUseCase = CreateAccountUseCase(userAccountRepository, gystClient, PasswordRules(), sessionManager)

    @Test
    fun can_process_CreateAccountActions() {
        val useCase = createUseCase()
        assertThat(
            useCase.canProcess(
                CreateAccountActions.CreateAccount(
                    "--email--",
                    "--password--"
                )
            )
        ).isTrue()
        assertThat(useCase.canProcess(CreateAccountActions.ValidatePasswordStrength("--password--"))).isTrue()
        assertThat(useCase.canProcess(object : Action {})).isFalse()
    }

    @Test
    fun handles_action__create_account__valid_creates_account() = runBlocking {
        val email = "foo@bar.com"
        val password = "password"
        val token = "--token--"
        val sessionManager: SessionManager = mockk(relaxUnitFun = true)
        mockkStatic("app.gyst.validation.PasswordRulesKt")
        every { password.isValidPassword(any()) } returns true
        val loginResponse = LoginResponse(UUID.randomUUID(), email, true, Instant.now(), Instant.now(), token)
        val gystClient = mockk<GystClient> {
            every {
                runBlocking {
                    createUserAccount(
                        CreateUserRequest(
                            email,
                            password
                        )
                    )
                }
            } returns loginResponse
        }

        val userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true)
        val useCase = createUseCase(userAccountRepository, gystClient, sessionManager)

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(CreateAccountActions.CreateAccount(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                CreateAccountResults.CreatingAccount,
                CreateAccountResults.AccountCreated
            )
        )

        verify {
            sessionManager.authenticatedWith(token)
            runBlocking { userAccountRepository.cacheAccount(loginResponse) }
        }
    }

    @Test
    fun handles_action__create_account__invalid_email() = runBlocking {
        val email = "foo@bar"
        val password = "password"
        val userAccountRepository = mockk<UserAccountRepository>(relaxUnitFun = true)
        val useCase = createUseCase(userAccountRepository)

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(CreateAccountActions.CreateAccount(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                CreateAccountResults.CreatingAccount,
                CreateAccountResults.InvalidEmail
            )
        )
    }

    @Test
    fun handles_action__create_account__invalid_password() = runBlocking {
        val email = "foo@bar.com"
        val password = "password"
        val userAccountRepository = mockk<UserAccountRepository>(relaxUnitFun = true)
        val useCase = createUseCase(userAccountRepository)
        mockkStatic("app.gyst.validation.PasswordRulesKt")
        val weaknesses = listOf(PasswordStrength.SpecialValues)
        every { any<String>().passwordWeaknesses(any()) } returns weaknesses
        every { any<String>().isValidPassword(any()) } returns false

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(CreateAccountActions.CreateAccount(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                CreateAccountResults.CreatingAccount,
                CreateAccountResults.PasswordValidationResults(weaknesses)
            )
        )

    }

    @Test
    fun handles_action__create_account__user_exists() = runBlocking {
        val email = "foo@bar.com"
        val password = "password"
        val gystClient: GystClient = mockk(relaxUnitFun = true) {
            every {
                runBlocking {
                    createUserAccount(
                        CreateUserRequest(
                            email,
                            password
                        )
                    )
                }
            } throws mockk<HttpException> {
                every { code() } returns 409
                every { message } returns null
            }
        }
        val useCase = createUseCase(gystClient = gystClient)
        mockkStatic("app.gyst.validation.PasswordRulesKt")
        every { password.isValidPassword(any()) } returns true

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(CreateAccountActions.CreateAccount(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                CreateAccountResults.CreatingAccount,
                CreateAccountResults.AccountExists
            )
        )
    }

    @Test
    fun handles_action__create_account__other_failure() = runBlocking {
        val email = "foo@bar.com"
        val password = "password"
        val gystClient:GystClient = mockk {
            every {
                runBlocking {
                    createUserAccount(
                        CreateUserRequest(
                            email,
                            password
                        )
                    )
                }
            } throws mockk<HttpException> {
                every { code() } returns 500
                every { message } returns null
            }
        }
        val useCase = createUseCase(gystClient = gystClient)
        mockkStatic("app.gyst.validation.PasswordRulesKt")
        every { password.isValidPassword(any()) } returns true

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(CreateAccountActions.CreateAccount(email, password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                CreateAccountResults.CreatingAccount,
                CreateAccountResults.CreationFailure
            )
        )
    }

    @Test
    fun handles_action__validate_password() = runBlocking {
        val password = "password"
        val useCase = createUseCase()
        mockkStatic("app.gyst.validation.PasswordRulesKt")
        val weaknesses = listOf(PasswordStrength.SpecialValues)
        every { any<String>().passwordWeaknesses(any()) } returns weaknesses

        val actualResults = mutableListOf<Result>()
        useCase.handleAction(CreateAccountActions.ValidatePasswordStrength(password)).collect {
            actualResults.add(it)
        }

        assertThat(actualResults).isEqualTo(
            listOf(
                CreateAccountResults.PasswordValidationResults(weaknesses)
            )
        )
    }
}