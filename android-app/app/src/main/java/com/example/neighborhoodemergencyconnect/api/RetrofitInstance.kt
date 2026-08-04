package com.example.neighborhoodemergencyconnect.api

import com.example.neighborhoodemergencyconnect.BuildConfig
import com.example.neighborhoodemergencyconnect.storage.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Use BuildConfig for easier overrides in different buildTypes
    private val BASE_URL: String = BuildConfig.BASE_URL

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()

        // If the request already has an Authorization header (explicit), don't overwrite it.
        if (original.header("Authorization") != null) {
            return@Interceptor chain.proceed(original)
        }

        val builder = original.newBuilder()
        TokenManager.token?.let {
            builder.header("Authorization", it)
        }
        val request = builder.build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
