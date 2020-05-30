package app.gyst.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.gyst.persistence.converter.DateConverter
import app.gyst.persistence.dao.UserDao
import app.gyst.persistence.dao.UserProfileDao
import app.gyst.persistence.model.User
import app.gyst.persistence.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Database(
    entities = [
        User::class,
        UserProfile::class
    ],
    version = 1
)
@TypeConverters(DateConverter::class)
@ExperimentalCoroutinesApi
abstract class GystDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val userProfileDao: UserProfileDao
}