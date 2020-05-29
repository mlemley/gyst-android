package app.gyst.persistence.converter


import androidx.room.TypeConverter
import app.gyst.common.toInstant
import app.gyst.common.toIso8601TimeStamp
import app.gyst.common.utc
import org.threeten.bp.Instant

object DateConverter {

    @TypeConverter
    @JvmStatic
    fun toInstant(date: String): Instant = date.toInstant()!!

    @TypeConverter
    @JvmStatic
    fun toString(instant: Instant): String = instant.utc().toIso8601TimeStamp()

}