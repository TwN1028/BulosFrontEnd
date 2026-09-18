package com.example.bulosfrontend

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.seconds

class TranslationRepository(
    private val context: Context,
    private val offlineManager: DictionaryManager,
) {
    private val api: TranslationApiService
    
    private val _isServerReady = MutableStateFlow(value = false)
    val isServerReady = _isServerReady.asStateFlow()

    private var lastApiError: String? = null

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val headerInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-Device-ID", "123e4567-e89b-42d3-a456-426614174000")
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(headerInterceptor)
            .protocols(listOf(Protocol.HTTP_1_1)) // Force HTTP/1.1 to avoid stream resets on Render
            .connectTimeout(60, TimeUnit.SECONDS) // Account for Render's potential cold start
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://bulosbackendserver.onrender.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(TranslationApiService::class.java)
    }

    suspend fun wakeUpServer() {
        if (!NetworkUtils.isOnline(context)) return
        
        // Loop until server is responsive or max attempts reached
        var attempts = 0
        while (attempts < 20) { // Try for ~1 minute (20 * 3s)
            try {
                val response = api.healthCheck()
                // In FastAPI, even a 404 for "/" means the server is AWAKE.
                if (response.isSuccessful || (response.code() == 404)) {
                    _isServerReady.value = true
                    return
                }
            } catch (_: Exception) {
                // Ignore errors during wake-up
            }
            attempts++
            delay(3.seconds) // Wait 3 seconds between pings
        }
        _isServerReady.value = false
    }

    suspend fun translate(text: String, source: UiLanguage, target: UiLanguage): String {
        if (text.uppercase().trim() == "DEBUG_DICT") {
            val online = NetworkUtils.isOnline(context)
            return "Mode: ${if (online) "Online (Render)" else "Offline (Local)"}, " +
                   "URL: https://bulosbackendserver.onrender.com/api/v1/translate/, " +
                   "Header X-Device-ID: Attached, " +
                   "ServerReady: ${isServerReady.value}, " +
                   "LastError: ${lastApiError ?: "None"}, " +
                   offlineManager.translate(text, source, target)
        }

        if (NetworkUtils.isOnline(context)) {
            // Wait for server to be ready if it's currently waking up
            if (!isServerReady.value) {
                withTimeoutOrNull(60000L) { // Wait max 60 seconds for Render cold start
                    while (!isServerReady.value) {
                        delay(1000L)
                    }
                }
            }

            if (isServerReady.value) {
                try {
                    val request = TranslationRequest(
                        text = text,
                        sourceLanguage = mapLanguageToCode(source),
                        targetLanguage = mapLanguageToCode(target)
                    )
                    val response = api.translate(request)
                    if (response.isSuccessful) {
                        val result = response.body()?.translated_text
                        if (result != null) {
                            lastApiError = null
                            TranslationState.translationSource = "Online"
                            return result
                        } else {
                            lastApiError = "Response body or text is null"
                        }
                    } else {
                        lastApiError = "HTTP ${response.code()}: ${response.errorBody()?.string() ?: "Unknown error"}"
                    }
                } catch (e: Exception) {
                    lastApiError = "Exception: ${e.message}"
                    e.printStackTrace()
                }
            }
        }
        
        // Fallback to offline logic
        TranslationState.translationSource = "Offline"
        return offlineManager.translate(text, source, target)
    }

    private fun mapLanguageToCode(language: UiLanguage): String = when (language) {
        UiLanguage.ENGLISH -> "en"
        UiLanguage.FILIPINO -> "tl"
        UiLanguage.BULOS -> "bul"
    }

    suspend fun syncOfflineModel(): Boolean {
        if (!NetworkUtils.isOnline(context)) return false
        
        try {
            val response = api.downloadOfflineData()
            if (response.isSuccessful) {
                val data = response.body() ?: return false
                val json = Gson().toJson(data)
                val file = File(context.filesDir, "custom_dictionary.json")
                withContext(Dispatchers.IO) {
                    FileOutputStream(file).use { it.write(json.toByteArray()) }
                }
                
                // Reload the offline manager with new data
                offlineManager.load(forceReload = true)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
