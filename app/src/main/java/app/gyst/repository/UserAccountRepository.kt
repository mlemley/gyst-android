package app.gyst.repository

import androidx.annotation.VisibleForTesting
import app.gyst.client.model.LoginResponse
import app.gyst.client.model.UserProfileResponse
import app.gyst.common.log
import app.gyst.persistence.dao.UserDao
import app.gyst.persistence.dao.UserProfileDao
import app.gyst.persistence.model.User
import app.gyst.persistence.model.UserProfile
import app.gyst.persistence.model.UserWithProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.threeten.bp.Instant


@ExperimentalCoroutinesApi
class UserAccountRepository(
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao
) {

    val userAccount: User? get() = userDao.user()
    val userWithProfile: UserWithProfile? get() = userAccount?.let { userDao.userWithProfileByUserId(it.id.toString()) }
    val hasAccount: Boolean get() = userDao.user() != null

    suspend fun cacheAccount(loginResponse: LoginResponse) {
        userDao.saveUserAccount(loginResponse.asUserModel())
    }

    suspend fun updateLastSeen(user: User) {
        userDao.update(user.copy(lastSeen = Instant.now()))
    }

    suspend fun saveUserProfile(userProfileResponse: UserProfileResponse) {
        userDao.byId(userProfileResponse.userId.toString())?.let {
            userProfileResponse.log()
            userProfileDao.saveUserProfile(userProfileResponse.asUserProfileModel())
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun LoginResponse.asUserModel(): User = User(id, email, active, accessToken, createAt, updatedAt)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun UserProfileResponse.asUserProfileModel(): UserProfile = UserProfile(id, userId, firstName, lastName, createdAt, updatedAt)

}
