package app.gyst.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import app.gyst.persistence.model.UserProfile

@Dao
abstract class UserProfileDao : BaseDao<UserProfile>() {

    @Query(
        """
        select * from user_profile where userId=:uuid limit 1
    """
    )
    abstract fun byUserId(uuid: String): UserProfile?

    @Query(
        """
        select * from user_profile where id=:uuid limit 1
    """
    )
    abstract fun byId(uuid: String): UserProfile?

    suspend fun saveUserProfile(userProfile: UserProfile) {
        byId(userProfile.id.toString())?.let {
            update(
                it.copy(
                    userId = userProfile.userId,
                    firstName = userProfile.firstName,
                    lastName = userProfile.lastName,
                    updatedAt = userProfile.updatedAt,
                    createdAt = userProfile.createdAt
                )
            )
        } ?: insert(userProfile)
    }
}
