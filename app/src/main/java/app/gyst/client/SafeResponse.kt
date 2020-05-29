package app.gyst.client

import okio.IOException
import retrofit2.HttpException

sealed class Status {
    object Success : Status()
    object Error : Status()
}

sealed class SafeResponse<T>(val status: Status, val data: T? = null, val message: String? = null) {
    class Success<T>(d: T?) : SafeResponse<T>(Status.Success, d)
    sealed class Error<T>(m: String) : SafeResponse<T>(status = Status.Error, message = m) {
        class Network<T>(m: String) : Error<T>(m)

        // 4xx Errors
        class BadRequest<T>(message: String?) : Error<T>(message ?: "Status: 400 - Bad Request")
        class NotAuthorized<T>(message: String?) : Error<T>(message ?: "Status 401 - Unauthorized")
        class Forbidden<T>(message: String?) : Error<T>(message ?: "Status 403 - Forbidden")
        class NotFound<T>(message: String?) : Error<T>(message ?: "Status 404 - Not Found")
        class Conflict<T>(message: String?) : Error<T>(message ?: "Status: 409 - Conflict")

        // 5xx Errors
        class InternalServerError<T>(message: String?) : Error<T>(message ?: "Status: 500 - Internal Server Error")
        class NotImplemented<T>(message: String?) : Error<T>(message ?: "Status: 501 - Internal Server Error")
        class BadGateway<T>(message: String?) : Error<T>(message ?: "Status: 502 - BadGateway")
        class ServiceUnavailable<T>(message: String?) : Error<T>(message ?: "Status: 503 - Service Unavailable")
        class GatewayTimeout<T>(message: String?) : Error<T>(message ?: "Status: 504 - GatewayTimeout")
        class VersionNotSupported<T>(message: String?) : Error<T>(message ?: "Status: 505 - Version Not Supported")

        class Unknown<T>(m: String) : Error<T>(m)
    }
}


suspend fun <T> safeCall(responseFunction: () -> T): SafeResponse<T> {
    return try {
        SafeResponse.Success(responseFunction.invoke())
    } catch (e: Exception) {
        when (e) {
            is HttpException -> e.code().toSafeError(e.message)
            is IOException -> SafeResponse.Error.Network(e.message ?: e.stackTrace.toString())
            else -> SafeResponse.Error.Unknown(e.message ?: e.stackTrace.toString())
        }
    }
}

fun <T> Int.toSafeError(message: String?): SafeResponse<T> = when (this) {
    // 4XX error
    400 -> SafeResponse.Error.BadRequest(message)
    401 -> SafeResponse.Error.NotAuthorized(message)
    403 -> SafeResponse.Error.Forbidden(message)
    404 -> SafeResponse.Error.NotFound(message)
    409 -> SafeResponse.Error.Conflict(message)

    // 5XX error
    500 -> SafeResponse.Error.InternalServerError(message)
    501 -> SafeResponse.Error.NotImplemented(message)
    502 -> SafeResponse.Error.BadGateway(message)
    503 -> SafeResponse.Error.ServiceUnavailable(message)
    504 -> SafeResponse.Error.GatewayTimeout(message)
    505 -> SafeResponse.Error.VersionNotSupported(message)

    else -> SafeResponse.Error.Unknown(this.toString())
}
