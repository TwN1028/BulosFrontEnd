package com.example.bulosfrontend

import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

data class TranslationRequest(
    @SerializedName("source_language") val sourceLanguage: String,
    @SerializedName("target_language") val targetLanguage: String,
    @SerializedName("text") val text: String,
)

data class TranslationResponse(
    @SerializedName("original_text") val originalText: String,
    @SerializedName("translated_text") val translatedText: String,
    @SerializedName("source_language") val sourceLanguage: String,
    @SerializedName("target_language") val targetLanguage: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("intermediate_language") val intermediateLanguage: String?,
    @SerializedName("translation_method") val translationMethod: String,
)

interface TranslationApi {
    @POST("api/v1/translate/")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse
}

object BackendLanguageCodes {
    private val codes = mapOf(
        "English" to "en",
        "Filipino" to "tl",
        "Bulos" to "bul",
    )

    fun forUiLabel(label: String): String =
        codes[label] ?: throw IllegalArgumentException("Unsupported language: $label")
}

interface InstallationIdStore {
    fun read(): String?
    fun write(value: String)
}

private class SharedPreferencesInstallationIdStore(context: Context) : InstallationIdStore {
    private val preferences = context.getSharedPreferences("installation_identity", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString("installation_uuid", null)

    override fun write(value: String) {
        preferences.edit().putString("installation_uuid", value).commit()
    }
}

class InstallationIdProvider(private val store: InstallationIdStore) {
    constructor(context: Context) : this(SharedPreferencesInstallationIdStore(context))

    @Synchronized
    fun get(): String {
        store.read()?.let { stored ->
            if (runCatching { UUID.fromString(stored) }.isSuccess) return stored
        }
        return UUID.randomUUID().toString().also { generated ->
            store.write(generated)
        }
    }
}

class DeviceIdInterceptor(private val installationIdProvider: InstallationIdProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request().newBuilder()
            .header("X-Device-ID", installationIdProvider.get())
            .build(),
    )
}

sealed interface TranslationResult {
    data class Success(val response: TranslationResponse) : TranslationResult
    data class Failure(val message: String) : TranslationResult
}

interface TranslationRepository {
    suspend fun translate(sourceLanguage: String, targetLanguage: String, text: String): TranslationResult
}

class NetworkTranslationRepository(private val api: TranslationApi) : TranslationRepository {
    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String,
    ): TranslationResult {
        val sourceCode = runCatching { BackendLanguageCodes.forUiLabel(sourceLanguage) }
            .getOrElse { return TranslationResult.Failure("The selected language is not supported.") }
        val targetCode = runCatching { BackendLanguageCodes.forUiLabel(targetLanguage) }
            .getOrElse { return TranslationResult.Failure("The selected language is not supported.") }
        return try {
        val response = api.translate(
            TranslationRequest(
                sourceLanguage = sourceCode,
                targetLanguage = targetCode,
                text = text,
            ),
        )
        require(response.originalText.isNotBlank())
        require(response.translatedText.isNotEmpty())
        require(response.sourceLanguage == sourceCode)
        require(response.targetLanguage == targetCode)
        require(response.translationMethod.isNotBlank())
        TranslationResult.Success(response)
    } catch (_: HttpException) {
        TranslationResult.Failure("Translation service is unavailable. Please try again.")
    } catch (_: IOException) {
        TranslationResult.Failure("Unable to connect. Check your internet and try again.")
    } catch (_: Exception) {
        TranslationResult.Failure("The translation response could not be read. Please try again.")
    }
    }
}

object TranslationServiceProvider {
    const val BASE_URL = "https://bulosbackendserver.onrender.com/"

    @Volatile
    private var repository: TranslationRepository? = null

    fun repository(context: Context): TranslationRepository = repository ?: synchronized(this) {
        repository ?: createRepository(context.applicationContext).also { repository = it }
    }

    private fun createRepository(context: Context): TranslationRepository {
        val client = OkHttpClient.Builder()
            .addInterceptor(DeviceIdInterceptor(InstallationIdProvider(context)))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApi::class.java)
        return NetworkTranslationRepository(api)
    }
}
