package app.gyst.client.serialization

import app.gyst.common.toIso8601TimeStamp
import app.gyst.common.utc
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.threeten.bp.Instant
import java.lang.reflect.Type

class InstantSerializer : JsonSerializer<Instant>{
    override fun serialize(src: Instant?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.utc()?.toIso8601TimeStamp())
    }

}
