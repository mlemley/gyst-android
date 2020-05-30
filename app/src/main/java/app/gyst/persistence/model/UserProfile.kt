package app.gyst.persistence.model

import androidx.room.*
import app.gyst.persistence.converter.DateConverter
import app.gyst.persistence.converter.UuidConverter
import org.threeten.bp.Instant
import java.util.*

@Entity(
    tableName = "user_profile",
    indices = [
        Index(name = "user_profile__id", value = ["id"], unique = true),
        Index(name = "user_profile__user_id", value = ["userId"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(
    DateConverter::class,
    UuidConverter::class
)
data class UserProfile(
    @PrimaryKey
    val id: UUID,
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
