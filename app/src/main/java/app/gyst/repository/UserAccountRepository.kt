package app.gyst.repository

import androidx.annotation.VisibleForTesting
import app.gyst.client.model.LoginResponse
import app.gyst.persistence.dao.UserDao
import app.gyst.persistence.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.threeten.bp.Instant


@ExperimentalCoroutinesApi
class UserAccountRepository(
    private val userDao: UserDao
) {

    val userAccount: User? get() = userDao.user()
    val hasAccount: Boolean get() = userDao.user() != null


    suspend fun cacheAccount(loginResponse: LoginResponse) {
        userDao.saveUserAccount(loginResponse.asUserModel())
    }

    suspend fun updateLastSeen(user: User) {
        userDao.update(user.copy(lastSeen = Instant.now()))
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun LoginResponse.asUserModel(): User = User(id, email, active, accessToken, createAt, updatedAt)

}
