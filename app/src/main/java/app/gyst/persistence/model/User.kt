package app.gyst.persistence.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.gyst.persistence.converter.DateConverter
import app.gyst.persistence.converter.UuidConverter
import org.threeten.bp.Instant
import java.util.*


@Entity(
    tableName = "user",
    indices = [
        Index(name = "user__id", value = ["id"], unique = true)
    ]
)
@TypeConverters(
    DateConverter::class,
    UuidConverter::class
)
data class User(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val isActive: Boolean,
    val accessToken: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastSeen: Instant = Instant.now()
)