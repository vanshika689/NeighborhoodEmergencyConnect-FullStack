package com.example.neighborhoodemergencyconnect.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue

object  RetrofitInstance {

    private const val BASE_URL = "https://cg-backend-2evk.onrender.com/"
    private val retrofit by lazy{ ///Lazy means dont create it immendiately create it only when needed
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : ApiService by lazy { ///ApiService'APIS usable becoz of this line
        retrofit.create(ApiService::class.java)
    }
    }


