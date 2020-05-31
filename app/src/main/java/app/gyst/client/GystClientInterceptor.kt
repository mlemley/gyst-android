package app.gyst.client

import app.gyst.common.log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class GystClientInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(signRequest(chain.request()))
    }

    private fun signRequest(request: Request): Request {
        val builder = request.newBuilder()
        sessionManager.authoriseRequest(builder)
        return builder.build().also {
            it.log()
        }
    }
}
