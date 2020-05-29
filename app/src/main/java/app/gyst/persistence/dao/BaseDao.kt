package app.gyst.persistence.dao


import androidx.annotation.WorkerThread
import androidx.room.*

@Dao
abstract class BaseDao<in Entity> {

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(t: Entity)

    @WorkerThread
    @Delete
    abstract fun delete(type: Entity)

    @WorkerThread
    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(type: Entity): Int

}