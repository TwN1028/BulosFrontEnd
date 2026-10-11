package com.example.bulosfrontend

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Isolated client for the backend Whisper endpoint. It is deliberately not wired
 * into the microphone, Vosk, Android recognizer, or translation pipelines.
 */
interface BackendSttApi {
    @Multipart
    @POST("api/v1/stt/transcribe")
    suspend fun transcribe(
        @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody? = null,
    ): BackendSttTranscriptionResponse
}

data class BackendSttTranscriptionResponse(
    @SerializedName("text") val text: String,
    @SerializedName("segments") val segments: List<JsonElement>,
    @SerializedName("language") val language: String,
    @SerializedName("language_probability") val languageProbability: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("error") val error: String? = null,
)

/** Factory helpers keep the API opt-in and allow callers to reuse existing OkHttp interceptors. */
object BackendSttClientFactory {
    private val supportedMimeTypes = setOf(
        "audio/wav", "audio/mpeg", "audio/ogg", "audio/flac", "audio/webm", "audio/mp4",
    )

    fun create(baseUrl: String, client: OkHttpClient): BackendSttApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendSttApi::class.java)

    fun audioPart(audioFile: File, mimeType: String): MultipartBody.Part {
        require(audioFile.isFile && audioFile.length() > 0L) { "Audio file must be non-empty." }
        require(mimeType in supportedMimeTypes) { "Unsupported STT audio MIME type: $mimeType" }
        return MultipartBody.Part.createFormData(
            "audio",
            audioFile.name,
            audioFile.asRequestBody(mimeType.toMediaType()),
        )
    }

    fun languagePart(language: String?): RequestBody? {
        if (language == null) return null
        require(language in setOf("bul", "en", "tl")) { "Unsupported STT language: $language" }
        return language.toRequestBody("text/plain".toMediaType())
    }
}
