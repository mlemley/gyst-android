package app.gyst.client


sealed class Session {
    object Anonymous : Session()
    data class Authenticated(val accessToken: String) : Session()
}

class SessionManager {

    var session: Session = Session.Anonymous
        private set

    fun authenticatedWith(token: String) {
        session = Session.Authenticated(token)
    }

}
