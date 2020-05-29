package app.gyst.client.serialization

import app.gyst.common.toInstant
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.threeten.bp.Instant
import java.lang.reflect.Type

class InstantDeserializer : JsonDeserializer<Instant>{
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Instant {
        return json?.asString?.let { date ->
            date.toInstant() ?: Instant.now()
        } ?: Instant.now()

    }
}
