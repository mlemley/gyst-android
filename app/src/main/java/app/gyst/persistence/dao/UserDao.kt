package app.gyst.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import app.gyst.persistence.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi


@Dao
@ExperimentalCoroutinesApi
abstract class UserDao : BaseDao<User>() {

    @Query(
        """
        select * from user order by datetime(lastSeen) desc limit 1 
    """
    )
    abstract fun user(): User?

    @Query("select * from user where id=:uuid limit 1")
    abstract fun byId(uuid: String): User?


    @Query("DELETE FROM user")
    abstract suspend fun deleteAll()

    suspend fun saveUserAccount(user: User) {
        byId(user.id.toString())?.let {
            update(
                it.copy(
                    isActive = user.isActive,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt,
                    lastSeen = user.lastSeen
                )
            )
        } ?: insert(user)
    }

}