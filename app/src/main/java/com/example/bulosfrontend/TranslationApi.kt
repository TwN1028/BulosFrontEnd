package com.example.bulosfrontend

import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.IOException
import java.net.SocketTimeoutException
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

interface TranslationSupportApi {
    @GET("api/v1/health/")
    suspend fun healthCheck(): Response<Unit>

    @GET("api/v1/dictionary/categories")
    suspend fun dictionaryCategories(): Response<List<DictionaryCategoryResponse>>

    @GET("api/v1/dictionary/category/{categoryKey}")
    suspend fun dictionaryCategory(
        @retrofit2.http.Path("categoryKey") categoryKey: String,
    ): Response<List<OfflineDictionaryEntryResponse>>
}

data class DictionaryCategoryResponse(
    @SerializedName("category_key") val categoryKey: String,
)

data class OfflineDictionaryEntryResponse(
    @SerializedName("english") val english: String?,
    @SerializedName("filipino") val filipino: String?,
    @SerializedName("bulos") val bulos: String?,
)

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
    suspend fun wakeUpServer() = Unit
    suspend fun loadOfflineModel() = Unit
    suspend fun syncOfflineModel(): Boolean = false
    val isServerReady: Boolean get() = true
    val isOfflineModelLoaded: Boolean get() = false
}

class NetworkTranslationRepository(private val api: TranslationApi) : TranslationRepository {
    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String,
    ): TranslationResult {
        if (!TranslationLanguageRules.isValidPair(sourceLanguage, targetLanguage)) {
            return TranslationResult.Failure("Source and target languages must be different.")
        }
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
    } catch (_: SocketTimeoutException) {
        TranslationResult.Failure("The translation server took too long to respond. Please try again shortly.")
    } catch (_: IOException) {
        TranslationResult.Failure("Unable to connect. Check your internet and try again.")
    } catch (_: Exception) {
        TranslationResult.Failure("The translation response could not be read. Please try again.")
    }
    }
}

class HybridTranslationRepository(
    private val context: Context,
    private val supportApi: TranslationSupportApi,
    private val networkRepository: NetworkTranslationRepository,
    private val offlineDictionary: OfflineDictionaryManager,
) : TranslationRepository {
    @Volatile
    override var isServerReady: Boolean = false
        private set
    override val isOfflineModelLoaded: Boolean
        get() = offlineDictionary.isLoaded

    override suspend fun wakeUpServer() {
        if (!NetworkUtils.isOnline(context)) {
            isServerReady = false
            return
        }
        repeat(20) {
            val awake = runCatching {
                val response = supportApi.healthCheck()
                response.isSuccessful
            }.getOrDefault(false)
            if (awake) {
                isServerReady = true
                return
            }
            kotlinx.coroutines.delay(3_000L)
        }
        isServerReady = false
    }

    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String,
    ): TranslationResult {
        val networkResult = if (NetworkUtils.isOnline(context)) {
            networkRepository.translate(sourceLanguage, targetLanguage, text)
        } else {
            TranslationResult.Failure("Unable to connect. Check your internet and try again.")
        }
        if (networkResult is TranslationResult.Success) {
            isServerReady = true
            return networkResult
        }
        val source = sourceLanguage.toUiLanguage()
        val target = targetLanguage.toUiLanguage()
        val offlineText = offlineDictionary.translate(text, source, target)
            ?: return networkResult
        return TranslationResult.Success(
            TranslationResponse(
                originalText = text,
                translatedText = offlineText,
                sourceLanguage = BackendLanguageCodes.forUiLabel(sourceLanguage),
                targetLanguage = BackendLanguageCodes.forUiLabel(targetLanguage),
                confidence = 1.0,
                intermediateLanguage = null,
                translationMethod = "offline_dictionary",
            ),
        )
    }

    override suspend fun syncOfflineModel(): Boolean {
        if (!NetworkUtils.isOnline(context)) return false
        return runCatching {
            val categoriesResponse = supportApi.dictionaryCategories()
            val categories = categoriesResponse.body()
            if (!categoriesResponse.isSuccessful || categories == null) return false
            val entries = buildList {
                categories.forEach { category ->
                    val response = supportApi.dictionaryCategory(category.categoryKey)
                    if (!response.isSuccessful) return false
                    response.body().orEmpty().forEach { entry ->
                        add(
                            buildMap {
                                entry.english?.takeIf(String::isNotBlank)?.let { put("English", it) }
                                entry.filipino?.takeIf(String::isNotBlank)?.let { put("Filipino", it) }
                                entry.bulos?.takeIf(String::isNotBlank)?.let { put("Bulos", it) }
                            },
                        )
                    }
                }
            }.filter(Map<String, String>::isNotEmpty)
            if (entries.isEmpty()) return false
            offlineDictionary.replace(entries)
            true
        }.getOrDefault(false)
    }

    override suspend fun loadOfflineModel() {
        offlineDictionary.load()
    }

    private fun String.toUiLanguage(): UiLanguage = when (lowercase()) {
        "filipino", "tagalog" -> UiLanguage.FILIPINO
        "bulos" -> UiLanguage.BULOS
        else -> UiLanguage.ENGLISH
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
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(TranslationApi::class.java)
        val supportApi = retrofit.create(TranslationSupportApi::class.java)
        val offlineDictionary = OfflineDictionaryManager(context)
        return HybridTranslationRepository(
            context = context,
            supportApi = supportApi,
            networkRepository = NetworkTranslationRepository(api),
            offlineDictionary = offlineDictionary,
        )
    }
}
