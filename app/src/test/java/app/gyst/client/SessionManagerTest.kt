package app.gyst.client

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Request

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

    @Test
    fun adds_authorization_header_to_request__when_authenticated() {
        val token = "--token--"
        val sessionManager = SessionManager()
        sessionManager.authenticatedWith(token)
        val requestBuilder = mockk<Request.Builder>(relaxed = true)

        sessionManager.authoriseRequest(requestBuilder)

        verify { requestBuilder.addHeader(AuthHeaderName, JwtFormat.format(token)) }
    }

    @Test
    fun does_not_adds_authorization_header_to_request__when_anonymous() {
        val sessionManager = SessionManager()
        val requestBuilder = mockk<Request.Builder>(relaxed = true)

        sessionManager.authoriseRequest(requestBuilder)

        verify(exactly = 0) { requestBuilder.addHeader(any(), any()) }
    }
}