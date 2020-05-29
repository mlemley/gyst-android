package app.gyst.client

import app.gyst.client.serialization.InstantDeserializer
import app.gyst.client.serialization.InstantSerializer
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.threeten.bp.Instant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private fun client(interceptor: Interceptor) = OkHttpClient().newBuilder()
    .addInterceptor(interceptor)
    .build()

fun gystClient(baseUrl: String, interceptor: Interceptor): GystClient = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(client(interceptor))
    .addConverterFactory(
        GsonConverterFactory.create(
            Gson().newBuilder()
                .registerTypeAdapter(Instant::class.java, InstantDeserializer())
                .registerTypeAdapter(Instant::class.java, InstantSerializer())

                .create()
        )
    )
    .build().create(GystClient::class.java)


