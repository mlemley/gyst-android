package app.gyst.client

import com.google.common.truth.Truth.assertThat

import org.junit.Test

class SessionManagerTest {

    @Test
    fun session_is_accessible() {
        assertThat(SessionManager().session).isEqualTo(Session.Anonymous)
    }

    @Test
    fun session_becomes_authenticated_with_access_token() {
        val token = "--token--"
        val sessionManager = SessionManager()

        sessionManager.authenticatedWith(token)

        assertThat(sessionManager.session).isEqualTo(Session.Authenticated(token))
    }
}