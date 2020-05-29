package app.gyst.client.model

data class CreateUserRequest(val email:String, val password:String)
data class LoginRequest(val email:String, val password:String)
