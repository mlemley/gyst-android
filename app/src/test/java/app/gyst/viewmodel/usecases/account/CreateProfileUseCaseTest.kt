package app.gyst.viewmodel.usecases.account

import app.gyst.client.GystClient
import app.gyst.client.model.UserProfileResponse
import app.gyst.client.model.UserProfileUpdateRequest
import app.gyst.common.viewmodel.Action
import app.gyst.common.viewmodel.Result
import app.gyst.repository.UserAccountRepository
import app.gyst.ui.onboarding.account.profile.CreateProfileValidationErrors.FirstNameEmpty
import app.gyst.ui.onboarding.account.profile.CreateProfileValidationErrors.LastNameEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.threeten.bp.Instant
import retrofit2.HttpException
import java.util.*

@ExperimentalCoroutinesApi
class CreateProfileUseCaseTest {

    private fun createUseCase(
        gystClient: GystClient = mockk(relaxUnitFun = true),
        userAccountRepository: UserAccountRepository = mockk(relaxUnitFun = true)
    ): CreateProfileUseCase = CreateProfileUseCase(gystClient, userAccountRepository)

    @Test
    fun handles_its_actions() {
        val useCase = createUseCase()
        assertThat(useCase.canProcess(CreateProfileActions.ProcessUsersName("first name", "last name"))).isTrue()
        assertThat(useCase.canProcess(object : Action {})).isFalse()
    }

    @Test
    fun handle_action__validates() = runBlocking {
        val useCase = createUseCase()
        val results = mutableListOf<Result>()

        useCase.handleAction(CreateProfileActions.ProcessUsersName("", "")).toList(results)
        assertThat(results).isEqualTo(listOf(CreateProfileResults.InputInvalid(listOf(FirstNameEmpty, LastNameEmpty))))

        results.clear()
        useCase.handleAction(CreateProfileActions.ProcessUsersName("Foo", "")).toList(results)
        assertThat(results).isEqualTo(listOf(CreateProfileResults.InputInvalid(listOf(LastNameEmpty))))

        results.clear()
        useCase.handleAction(CreateProfileActions.ProcessUsersName("", "Bar")).toList(results)
        assertThat(results).isEqualTo(listOf(CreateProfileResults.InputInvalid(listOf(FirstNameEmpty))))
    }

    @Test
    fun handle_action__creates_profile() = runBlocking {
        val firstName = "--first-name--"
        val lastName = "--last-name--"
        val userProfileResponse =
            UserProfileResponse(UUID.randomUUID(), UUID.randomUUID(), firstName, lastName, Instant.now(), Instant.now())
        val gystClient: GystClient = mockk {
            every { runBlocking { createUserProfile(UserProfileUpdateRequest(firstName, lastName)) } } returns userProfileResponse
        }
        val userAccountRepository = mockk<UserAccountRepository>(relaxUnitFun = true)
        val useCase = createUseCase(gystClient, userAccountRepository)
        val results = mutableListOf<Result>()

        useCase.handleAction(CreateProfileActions.ProcessUsersName(firstName, lastName)).toList(results)

        assertThat(results).isEqualTo(listOf(CreateProfileResults.ProfileCreated))
        verify {
            runBlocking { userAccountRepository.saveUserProfile(userProfileResponse) }
        }
    }

    @Test
    fun handle_action__updates_profile__when_conflict() {
        TODO("Not yet implemented")
    }

    @Test
    fun handle_action__communicates_failure() = runBlocking {
        val firstName = "--first-name--"
        val lastName = "--last-name--"
        val gystClient: GystClient = mockk {
            every {
                runBlocking {
                    createUserProfile(
                        UserProfileUpdateRequest(
                            firstName,
                            lastName
                        )
                    )
                }
            } throws mockk<HttpException>(relaxed = true) {
                every { code() } returns 500
                every { message } returns null
            }
        }
        val useCase = createUseCase(gystClient)
        val results = mutableListOf<Result>()

        useCase.handleAction(CreateProfileActions.ProcessUsersName(firstName, lastName)).toList(results)

        assertThat(results).isEqualTo(listOf(CreateProfileResults.CreateProfileFailed))
    }
}