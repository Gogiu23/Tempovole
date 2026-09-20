package com.example.tiempo.data

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 8,
        @Query("language") language: String = "es",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

@Serializable
data class GeocodingResponse(val results: List<GeocodingResult>? = null)

@Serializable
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
) {
    val displayName: String
        get() = listOfNotNull(name, admin1, country).distinct().joinToString(", ")
}

class GeocodingRepository(private val api: GeocodingApi = Network.geocodingApi) {
    suspend fun search(query: String): List<GeocodingResult> =
        if (query.isBlank()) emptyList() else api.search(name = query).results.orEmpty()
}
