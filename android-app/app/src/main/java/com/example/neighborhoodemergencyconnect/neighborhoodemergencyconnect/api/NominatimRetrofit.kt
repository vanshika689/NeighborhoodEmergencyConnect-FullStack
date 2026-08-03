package com.example.neighborhoodemergencyconnect.api
import com.example.neighborhoodemergencyconnect.models.ReverseGeocodeResponse
import com.example.neighborhoodemergencyconnect.models.SearchLocationResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimApi {

    @Headers(
        "User-Agent: NeighborhoodEmergencyConnect"
    )
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2"
    ): Response<ReverseGeocodeResponse>

    @Headers(
        "User-Agent: NeighborhoodEmergencyConnect"
    )
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("countrycodes") country: String = "in",
        @Query("addressdetails") address: Int = 1,
        @Query("limit") limit: Int = 5
    ): Response<List<SearchLocationResponse>>
}

object NominatimRetrofit {

    val api: NominatimApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(NominatimApi::class.java)
    }
}