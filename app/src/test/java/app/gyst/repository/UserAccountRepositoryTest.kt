package app.gyst.repository

import app.gyst.client.model.LoginResponse
import app.gyst.client.model.UserProfileResponse
import app.gyst.common.toInstant
import app.gyst.persistence.dao.UserDao
import app.gyst.persistence.dao.UserProfileDao
import app.gyst.persistence.model.User
import app.gyst.persistence.model.UserProfile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.threeten.bp.Instant
import java.util.*

@ExperimentalCoroutinesApi
class UserAccountRepositoryTest {

    private fun createRepository(
        userDao: UserDao = mockk(relaxUnitFun = true) {
            every { user() } returns null
        },
        userProfileDao: UserProfileDao = mockk(relaxUnitFun = true)
    ): UserAccountRepository = UserAccountRepository(userDao, userProfileDao)

    @Test
    fun provides_access_to_user() {
        val userDao: UserDao = mockk() {
            every { user() } returns null andThen mockk<User>()
        }

        val repository = createRepository(userDao = userDao)

        assertThat(repository.userAccount).isNull()
        assertThat(repository.userAccount).isNotNull()
    }

    @Test
    fun provides_access_to_user_with_profile() {
        val user = User(UUID.randomUUID(), "", true, "", Instant.now(), Instant.now())
        val userDao: UserDao = mockk() {
            every { user() } returns user
            every { userWithProfileByUserId(user.id.toString()) } returns mockk()
        }

        val repository = createRepository(userDao = userDao)

        assertThat(repository.userWithProfile).isNotNull()
    }

    @Test
    fun has_account() {
        val userDao: UserDao = mockk() {
            every { user() } returns null andThen mockk<User>()
        }

        val repository = createRepository(userDao = userDao)

        assertThat(repository.hasAccount).isFalse()
        assertThat(repository.hasAccount).isTrue()
    }

    @Test
    fun cache_account__adapts__login_response__to__user__then_saves_it() = runBlocking {
        val dao: UserDao = mockk(relaxed = true)
        val repository = createRepository(userDao = dao)
        val loginResponse = LoginResponse(UUID.randomUUID(), "foo@bar.com", true, Instant.now(), Instant.now(), "--access-token--")

        repository.cacheAccount(loginResponse)

        verify {
            runBlocking { dao.saveUserAccount(any()) }
        }
    }

    @Test
    fun updates_last_seen() = runBlocking {
        val then = 1590590375821.toInstant()
        val now = Instant.now()
        mockkStatic(Instant::class)
        every { Instant.now() } returns now
        val user = User(UUID.randomUUID(), "foo@bar.com", true, "--token--", then, then, then)
        val dao: UserDao = mockk(relaxed = true)
        val repository = createRepository(userDao = dao)

        repository.updateLastSeen(user)

        verify {
            runBlocking { dao.update(user.copy(lastSeen = now)) }
        }
    }

    @Test
    fun saves_user_profile_response_for_user() = runBlocking {
        val user = User(UUID.randomUUID(), "--email--", true, "--token--", Instant.now(), Instant.now(), Instant.now())
        val userProfileResponse =
            UserProfileResponse(UUID.randomUUID(), user.id, "--first-name--", "--last-name--", Instant.now(), Instant.now())
        val userDao: UserDao = mockk {
            every { byId(user.id.toString()) } returns user
        }
        val userProfileDao: UserProfileDao = mockk(relaxUnitFun = true)
        val repository = createRepository(userDao, userProfileDao)

        repository.saveUserProfile(userProfileResponse)

        verify {
            runBlocking {
                userProfileDao.saveUserProfile(
                    UserProfile(
                        userProfileResponse.id,
                        userProfileResponse.userId,
                        userProfileResponse.firstName,
                        userProfileResponse.lastName,
                        userProfileResponse.createdAt,
                        userProfileResponse.updatedAt
                    )
                )
            }
        }
    }

}