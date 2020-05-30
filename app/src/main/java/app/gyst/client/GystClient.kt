package app.gyst.client

import app.gyst.client.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface GystClient {

    @POST("/v1/login/")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("/v1/user/")
    suspend fun createUserAccount(@Body createUserRequest: CreateUserRequest): LoginResponse

    @POST("/v1/user/profile/")
    suspend fun createUserProfile(@Body userProfileUpdateRequest: UserProfileUpdateRequest): UserProfileResponse
}
