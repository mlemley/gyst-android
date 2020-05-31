package app.gyst.client.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant
import java.util.*


data class LoginResponse(
    val id: UUID,
    val email: String,
    val active: Boolean,
    @SerializedName("created_at")
    val createAt: Instant,
    @SerializedName("updated_at")
    val updatedAt: Instant,
    @SerializedName("access_token")
    val accessToken: String,
    val profile: UserProfileResponse? = null
)

@Parcelize
data class UserProfileResponse(
    val id: UUID,
    @SerializedName("user_id")
    val userId: UUID,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("created_at")
    val createdAt: Instant,

    @SerializedName("updated_at")
    val updatedAt: Instant
) : Parcelable
