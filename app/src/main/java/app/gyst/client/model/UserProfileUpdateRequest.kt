package app.gyst.client.model

import com.google.gson.annotations.SerializedName

data class UserProfileUpdateRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String
)
