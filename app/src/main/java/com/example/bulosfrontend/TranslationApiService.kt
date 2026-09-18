package com.example.bulosfrontend

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class TranslationRequest(
    @SerializedName("text") val text: String,
    @SerializedName("source_language") val sourceLanguage: String,
    @SerializedName("target_language") val targetLanguage: String
)

data class TranslationResponse(
    @SerializedName("translated_text") val translated_text: String?,
    @SerializedName("engine") val engine: String?
)

interface TranslationApiService {
    @GET("/")
    suspend fun healthCheck(): Response<Unit>

    @POST("api/v1/translate/")
    suspend fun translate(@Body request: TranslationRequest): Response<TranslationResponse>

    @GET("api/v1/sync/offline-data/")
    suspend fun downloadOfflineData(): Response<List<Map<String, String>>>
}
