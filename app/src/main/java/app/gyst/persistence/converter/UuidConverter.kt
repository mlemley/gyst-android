package app.gyst.persistence.converter

import androidx.room.TypeConverter
import java.util.*


object UuidConverter {
    @TypeConverter
    @JvmStatic
    fun toUUID(id:String?):UUID? = if(id == null) null else UUID.fromString(id)

    @TypeConverter
    @JvmStatic
    fun toString(id:UUID):String = id.toString()

}