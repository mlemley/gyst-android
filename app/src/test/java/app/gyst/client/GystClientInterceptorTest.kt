package app.gyst.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.Test

class GystClientInterceptorTest {

    private fun createInterceptor(
        sessionManager: SessionManager = mockk(relaxUnitFun = true)
    ): GystClientInterceptor = GystClientInterceptor(sessionManager)

    @Test
    fun adds_authorization_header__when_authenticated() {
        val sessionManager: SessionManager = mockk(relaxUnitFun = true)
        val requestBuilder = mockk<Request.Builder>(relaxed = true)
        val chain  = mockk<Interceptor.Chain> {
            every { proceed(any()) } returns mockk()
            every { request() } returns mockk {
                every { newBuilder() } returns requestBuilder
            }
        }

        createInterceptor(sessionManager).intercept(chain)

        verify {
            sessionManager.authoriseRequest(requestBuilder)
        }
    }
}