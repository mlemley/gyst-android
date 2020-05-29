package app.gyst.client

import app.gyst.common.log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class GystClientInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(signRequest(chain.request()))
    }

    private fun signRequest(request: Request): Request {
        val builder = request.newBuilder()
/*
        when (request.method) {
            "POST" -> {
                val json = JsonParser.parseString(request.body?.asString ?: "{}").asJsonObject
                builder.post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            }
        }
*/
        return builder.build().also {
            it.log()
        }
    }
}
