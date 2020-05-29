package app.gyst.repository

import app.gyst.client.model.LoginResponse
import app.gyst.common.toInstant
import app.gyst.persistence.dao.UserDao
import app.gyst.persistence.model.User
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
        }
    ): UserAccountRepository = UserAccountRepository(userDao)

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
}