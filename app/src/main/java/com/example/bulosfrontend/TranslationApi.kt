package com.example.bulosfrontend

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import retrofit2.http.Query
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

internal val BACKEND_HEALTH_RETRY_DELAYS_MS = longArrayOf(0L, 2_000L, 4_000L)

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
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
    ): Response<List<OfflineDictionaryEntryResponse>>
}

data class DictionaryCategoryResponse(
    @SerializedName("category_key") val categoryKey: String,
    @SerializedName(value = "entry_count", alternate = ["count", "total", "total_count"])
    val entryCount: Int = 0,
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
    fun write(value: String): Boolean
}

private class SharedPreferencesInstallationIdStore(context: Context) : InstallationIdStore {
    private val preferences = context.getSharedPreferences("installation_identity", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString("installation_uuid", null)

    @SuppressLint("UseKtx")
    override fun write(value: String): Boolean =
        preferences.edit().putString("installation_uuid", value).commit()
}

class InstallationIdProvider(private val store: InstallationIdStore) {
    constructor(context: Context) : this(SharedPreferencesInstallationIdStore(context))

    private var currentId: String? = null
    private var needsPersistence = false

    @Synchronized
    fun get(): String {
        currentId?.let { id ->
            if (needsPersistence) needsPersistence = !store.write(id)
            return id
        }

        val storedId = store.read()
        val id = storedId?.takeIf(::isUuidV4) ?: UUID.randomUUID().toString()
        currentId = id
        if (storedId != id) {
            needsPersistence = !store.write(id)
        }
        return id
    }

    private fun isUuidV4(value: String): Boolean {
        if (!UUID_PATTERN.matches(value)) return false
        return runCatching { UUID.fromString(value).version() == 4 }.getOrDefault(false)
    }

    private companion object {
        val UUID_PATTERN = Regex(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            RegexOption.IGNORE_CASE,
        )
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
    data class Suggestions(val candidates: List<OfflineTranslationSuggestion>) : TranslationResult
    data class Failure(val message: String) : TranslationResult
}

interface TranslationRepository {
    suspend fun translate(sourceLanguage: String, targetLanguage: String, text: String): TranslationResult
    suspend fun wakeUpServer() = Unit
    suspend fun loadOfflineModel() = Unit
    suspend fun syncOfflineModel(): Boolean = false
    fun setOfflineModeEnabled(enabled: Boolean) = Unit
    val isServerReady: Boolean get() = true
    val isOfflineModelLoaded: Boolean get() = false
    val offlineDictionaryEntries: List<DictionaryEntry> get() = emptyList()
}

class NetworkTranslationRepository(private val api: TranslationApi) : TranslationRepository {
    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String,
    ): TranslationResult {
        if (!TranslationLanguageRules.isValidPair(sourceLanguage, targetLanguage)) {
            return TranslationResult.Failure("This language pair is not supported.")
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
    } catch (error: HttpException) {
        if (error.isDeviceIdError()) {
            TranslationResult.Failure(DEVICE_ID_ERROR_MESSAGE)
        } else {
            TranslationResult.Failure("Translation service is unavailable. Please try again.")
        }
    } catch (_: SocketTimeoutException) {
        TranslationResult.Failure("The translation server took too long to respond. Please try again shortly.")
    } catch (_: IOException) {
        TranslationResult.Failure("Unable to connect. Check your internet and try again.")
    } catch (_: Exception) {
        TranslationResult.Failure("The translation response could not be read. Please try again.")
    }
    }
}

private const val DEVICE_ID_ERROR_MESSAGE =
    "This installation could not be identified. Restart the app and try again."

private fun HttpException.isDeviceIdError(): Boolean {
    if (code() != 400) return false
    val details = runCatching { response()?.errorBody()?.string()?.lowercase().orEmpty() }
        .getOrDefault("")
    val identifiesDeviceId = listOf("x-device-id", "device_id", "device id", "device-id")
        .any(details::contains)
    val identifiesValidationFailure = listOf("missing", "required", "invalid", "uuid", "malformed")
        .any(details::contains)
    return identifiesDeviceId && identifiesValidationFailure
}

class HybridTranslationRepository(
    private val context: Context,
    private val supportApi: TranslationSupportApi,
    private val healthApi: TranslationSupportApi,
    private val networkRepository: NetworkTranslationRepository,
    private val offlineDictionary: OfflineDictionaryManager,
) : TranslationRepository {
    @Volatile
    private var offlineModeEnabled: Boolean = false
    @Volatile
    override var isServerReady: Boolean = false
        private set
    override val isOfflineModelLoaded: Boolean
        get() = offlineDictionary.isLoaded
    override val offlineDictionaryEntries: List<DictionaryEntry>
        get() = offlineDictionary.entries()

    override fun setOfflineModeEnabled(enabled: Boolean) {
        offlineModeEnabled = enabled
        if (enabled) isServerReady = false
    }

    override suspend fun wakeUpServer() {
        if (!canUseOnlineServices(NetworkUtils.isOnline(context), offlineModeEnabled)) {
            isServerReady = false
            return
        }

        for (retryDelayMs in BACKEND_HEALTH_RETRY_DELAYS_MS) {
            if (retryDelayMs > 0L) delay(retryDelayMs)
            if (!canUseOnlineServices(NetworkUtils.isOnline(context), offlineModeEnabled)) {
                isServerReady = false
                return
            }

            val ready = try {
                healthApi.healthCheck().isSuccessful
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                false
            }
            if (ready) {
                isServerReady = true
                return
            }
        }
        isServerReady = false
    }

    override suspend fun translate(
        sourceLanguage: String,
        targetLanguage: String,
        text: String,
    ): TranslationResult {
        val source = sourceLanguage.toUiLanguage()
        val target = targetLanguage.toUiLanguage()
        if (source == UiLanguage.BULOS || target == UiLanguage.BULOS) {
            when (val verifiedResult = offlineDictionary.lookup(text, source, target)) {
                is OfflineLookupResult.Translation -> {
                    return offlineTranslationSuccess(verifiedResult, text, sourceLanguage, targetLanguage)
                }
                is OfflineLookupResult.Suggestions -> return TranslationResult.Suggestions(verifiedResult.candidates)
                OfflineLookupResult.Unavailable -> {
                    return TranslationResult.Failure(DIRECT_BULOS_TRANSLATION_UNAVAILABLE_MESSAGE)
                }
                OfflineLookupResult.NoData -> Unit
            }
        }

        val wasOnline = canUseOnlineServices(NetworkUtils.isOnline(context), offlineModeEnabled)
        val networkResult = if (wasOnline) {
            networkRepository.translate(sourceLanguage, targetLanguage, text)
        } else {
            TranslationResult.Failure("Unable to connect. Check your internet and try again.")
        }
        if (networkResult is TranslationResult.Success) {
            isServerReady = true
            return networkResult
        }
        return when (val offlineResult = offlineDictionary.lookup(text, source, target)) {
            is OfflineLookupResult.Translation ->
                offlineTranslationSuccess(offlineResult, text, sourceLanguage, targetLanguage)
            is OfflineLookupResult.Suggestions -> TranslationResult.Suggestions(offlineResult.candidates)
            OfflineLookupResult.NoData -> unresolvedFallback(networkResult, wasOnline, hasOfflineData = false)
            OfflineLookupResult.Unavailable -> unresolvedFallback(
                networkResult = networkResult,
                wasOnline = wasOnline,
                hasOfflineData = true,
                unavailableMessage = if (source == UiLanguage.BULOS || target == UiLanguage.BULOS) {
                    DIRECT_BULOS_TRANSLATION_UNAVAILABLE_MESSAGE
                } else {
                    DEFAULT_TRANSLATION_UNAVAILABLE_MESSAGE
                },
            )
        }
    }

    override suspend fun syncOfflineModel(): Boolean {
        if (!canUseOnlineServices(NetworkUtils.isOnline(context), offlineModeEnabled)) return false
        return runCatching {
            val categoriesResponse = supportApi.dictionaryCategories()
            val categories = categoriesResponse.body()
            if (!categoriesResponse.isSuccessful || categories == null) return false
            val entries = refreshCompleteDictionary(
                categories = categories,
                fetchPage = { categoryKey, skip, limit ->
                    if (offlineModeEnabled) throw CancellationException("Offline Mode enabled")
                    val response = supportApi.dictionaryCategory(categoryKey, skip, limit)
                    if (!response.isSuccessful) {
                        throw IOException("Dictionary page failed with HTTP ${response.code()}")
                    }
                    response.body().orEmpty()
                },
                logger = { Log.d(DICTIONARY_SYNC_TAG, it) },
                save = { downloadedEntries -> offlineDictionary.replace(downloadedEntries) },
            )
            if (entries.isEmpty()) return false
            Log.i(DICTIONARY_SYNC_TAG, "Saved ${entries.size} dictionary entries locally")
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

    private fun offlineTranslationSuccess(
        result: OfflineLookupResult.Translation,
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
    ) = TranslationResult.Success(
        TranslationResponse(
            originalText = text,
            translatedText = result.translatedText,
            sourceLanguage = BackendLanguageCodes.forUiLabel(sourceLanguage),
            targetLanguage = BackendLanguageCodes.forUiLabel(targetLanguage),
            confidence = result.confidence,
            intermediateLanguage = null,
            translationMethod = result.method,
        ),
    )
}

internal fun canUseOnlineServices(hasInternetConnection: Boolean, offlineModeEnabled: Boolean): Boolean =
    hasInternetConnection && !offlineModeEnabled

internal fun unresolvedFallback(
    networkResult: TranslationResult,
    wasOnline: Boolean,
    hasOfflineData: Boolean,
    unavailableMessage: String = DEFAULT_TRANSLATION_UNAVAILABLE_MESSAGE,
): TranslationResult = when {
    wasOnline || networkResult.isDeviceIdFailure() -> networkResult
    !hasOfflineData -> TranslationResult.Failure(
        "Offline translation data is not installed. Connect to the internet once to download it.",
    )
    else -> TranslationResult.Failure(unavailableMessage)
}

private const val DEFAULT_TRANSLATION_UNAVAILABLE_MESSAGE = "Translation unavailable."
internal const val DIRECT_BULOS_TRANSLATION_UNAVAILABLE_TITLE = "No direct Bulos equivalent found"
internal const val DIRECT_BULOS_TRANSLATION_UNAVAILABLE_DESCRIPTION =
    "This term has no documented Bulos translation."
internal const val DIRECT_BULOS_TRANSLATION_UNAVAILABLE_MESSAGE =
    "$DIRECT_BULOS_TRANSLATION_UNAVAILABLE_TITLE\n$DIRECT_BULOS_TRANSLATION_UNAVAILABLE_DESCRIPTION"

internal fun isDirectBulosTranslationUnavailable(message: String?): Boolean =
    message == DIRECT_BULOS_TRANSLATION_UNAVAILABLE_MESSAGE

internal data class TranslationFailurePresentation(
    val title: String,
    val description: String,
)

internal fun translationFailurePresentation(message: String): TranslationFailurePresentation = when (message) {
    DIRECT_BULOS_TRANSLATION_UNAVAILABLE_MESSAGE -> TranslationFailurePresentation(
        title = DIRECT_BULOS_TRANSLATION_UNAVAILABLE_TITLE,
        description = DIRECT_BULOS_TRANSLATION_UNAVAILABLE_DESCRIPTION,
    )
    "Unable to connect. Check your internet and try again." -> TranslationFailurePresentation(
        title = "No internet connection",
        description = "Check your connection and try again.",
    )
    "Offline translation data is not installed. Connect to the internet once to download it." -> TranslationFailurePresentation(
        title = "Offline translation data unavailable",
        description = "Connect to the internet once to download the dictionary data.",
    )
    "Translation service is unavailable. Please try again." -> TranslationFailurePresentation(
        title = "Translation service unavailable",
        description = "Please try again in a moment.",
    )
    "The translation server took too long to respond. Please try again shortly." -> TranslationFailurePresentation(
        title = "Translation service is taking too long",
        description = "Please try again shortly.",
    )
    DEVICE_ID_ERROR_MESSAGE -> TranslationFailurePresentation(
        title = "App identity unavailable",
        description = "Restart the app and try again.",
    )
    "This language pair is not supported." -> TranslationFailurePresentation(
        title = "Unsupported language pair",
        description = "Choose a supported source and target language.",
    )
    "The selected language is not supported." -> TranslationFailurePresentation(
        title = "Unsupported language",
        description = "Choose one of the available languages.",
    )
    "The translation response could not be read. Please try again." -> TranslationFailurePresentation(
        title = "Translation response unavailable",
        description = "Please try again.",
    )
    else -> TranslationFailurePresentation(
        title = "Translation couldn't be completed",
        description = message.ifBlank { "Please try again." },
    )
}

private fun TranslationResult.isDeviceIdFailure(): Boolean =
    this is TranslationResult.Failure && message == DEVICE_ID_ERROR_MESSAGE

private const val DICTIONARY_PAGE_SIZE = 100
private const val DICTIONARY_SYNC_TAG = "DictionarySync"

/** Downloads all declared pages before the existing cache is replaced. */
internal suspend fun downloadCompleteDictionary(
    categories: List<DictionaryCategoryResponse>,
    pageSize: Int = DICTIONARY_PAGE_SIZE,
    fetchPage: suspend (categoryKey: String, skip: Int, limit: Int) -> List<OfflineDictionaryEntryResponse>,
    logger: (String) -> Unit = {},
): List<Map<String, String>> {
    require(pageSize > 0)
    val entries = mutableListOf<Map<String, String>>()

    categories.forEach { category ->
        var skip = 0
        var downloaded = 0
        logger("Category=${category.categoryKey}, declaredTotal=${category.entryCount}")
        do {
            val page = fetchPage(category.categoryKey, skip, pageSize)
            logger(
                "Category=${category.categoryKey}, declaredTotal=${category.entryCount}, " +
                    "skip=$skip, limit=$pageSize, returned=${page.size}",
            )
            val remaining = if (category.entryCount > 0) {
                (category.entryCount - downloaded).coerceAtLeast(0)
            } else {
                page.size
            }
            page.take(remaining).forEach { entry ->
                val mapped = buildMap {
                    put(OfflineDictionary.CATEGORY_KEY, category.categoryKey)
                    entry.english?.takeIf(String::isNotBlank)?.let { put("English", it) }
                    entry.filipino?.takeIf(String::isNotBlank)?.let { put("Filipino", it) }
                    entry.bulos?.takeIf(String::isNotBlank)?.let { put("Bulos", it) }
                }
                if (mapped.size > 1) {
                    entries += mapped
                }
            }
            downloaded += page.size
            skip += page.size
        } while (page.isNotEmpty() && (category.entryCount <= 0 || downloaded < category.entryCount))
        logger("Category=${category.categoryKey}, downloaded=$downloaded")
    }
    return entries
}

internal suspend fun refreshCompleteDictionary(
    categories: List<DictionaryCategoryResponse>,
    pageSize: Int = DICTIONARY_PAGE_SIZE,
    fetchPage: suspend (categoryKey: String, skip: Int, limit: Int) -> List<OfflineDictionaryEntryResponse>,
    logger: (String) -> Unit = {},
    save: suspend (List<Map<String, String>>) -> Unit,
): List<Map<String, String>> {
    val entries = downloadCompleteDictionary(categories, pageSize, fetchPage, logger)
    if (entries.isNotEmpty()) save(entries)
    return entries
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
        val healthClient = client.newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .build()
        val healthRetrofit = retrofit.newBuilder()
            .client(healthClient)
            .build()
        val api = retrofit.create(TranslationApi::class.java)
        val supportApi = retrofit.create(TranslationSupportApi::class.java)
        val healthApi = healthRetrofit.create(TranslationSupportApi::class.java)
        val offlineDictionary = OfflineDictionaryManager(context)
        return HybridTranslationRepository(
            context = context,
            supportApi = supportApi,
            healthApi = healthApi,
            networkRepository = NetworkTranslationRepository(api),
            offlineDictionary = offlineDictionary,
        )
    }
}
