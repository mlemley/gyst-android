package app.gyst.client

import okhttp3.Request

const val AuthHeaderName = "Authorization"
const val JwtFormat = "Bearer %s"

interface Authorise {
    fun authorise(requestBuilder: Request.Builder) {}
}

sealed class Session:Authorise {
    object Anonymous : Session()
    data class Authenticated(val accessToken: String) : Session() {
        override fun authorise(requestBuilder: Request.Builder) {
            requestBuilder.addHeader(AuthHeaderName, JwtFormat.format(accessToken))
        }
    }
}


class SessionManager {

    var session: Session = Session.Anonymous
        private set

    fun authenticatedWith(token: String) {
        session = Session.Authenticated(token)
    }

    fun authoriseRequest(requestBuilder: Request.Builder) {
        session.authorise(requestBuilder)
    }


}
