package com.pkstudy.hajiribook.data.remote.interceptor

import com.pkstudy.hajiribook.data.remote.api.HajiriApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        val token = tokenProvider()
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }
        builder.header("Accept", "application/json")
        builder.header("Content-Type", "application/json")
        return chain.proceed(builder.build())
    }
}

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2/hajiri-api/api/"

    fun create(
        baseUrl: String = DEFAULT_BASE_URL,
        tokenProvider: () -> String? = { null }
    ): HajiriApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HajiriApiService::class.java)
    }
}
