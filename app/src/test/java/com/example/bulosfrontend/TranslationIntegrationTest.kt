package com.example.bulosfrontend

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.UUID

class TranslationIntegrationTest {
    @Test
    fun languageMappingUsesExactBackendCodes() {
        assertEquals("en", BackendLanguageCodes.forUiLabel("English"))
        assertEquals("tl", BackendLanguageCodes.forUiLabel("Filipino"))
        assertEquals("bul", BackendLanguageCodes.forUiLabel("Bulos"))
    }

    @Test
    fun filipinoRequestUsesTlAndFallbackOriginalIsSuccess() = runBlocking {
        val api = RecordingApi(
            TranslationResponse(
                originalText = "Hindi kilala",
                translatedText = "Hindi kilala",
                sourceLanguage = "tl",
                targetLanguage = "bul",
                confidence = 0.0,
                intermediateLanguage = null,
                translationMethod = "fallback_original",
            ),
        )

        val result = NetworkTranslationRepository(api).translate("Filipino", "Bulos", "Hindi kilala")

        assertEquals("tl", api.lastRequest?.sourceLanguage)
        assertEquals("bul", api.lastRequest?.targetLanguage)
        assertTrue(result is TranslationResult.Success)
        assertEquals("Hindi kilala", (result as TranslationResult.Success).response.translatedText)
    }

    @Test
    fun englishToBulosStoresBackendResponseWithoutLocalTranslation() = runBlocking {
        val api = RecordingApi(
            TranslationResponse(
                originalText = "Beautiful",
                translatedText = "masampat",
                sourceLanguage = "en",
                targetLanguage = "bul",
                confidence = 1.0,
                intermediateLanguage = null,
                translationMethod = "dictionary",
            ),
        )

        val result = NetworkTranslationRepository(api).translate("English", "Bulos", "Beautiful")

        assertEquals("en", api.lastRequest?.sourceLanguage)
        assertEquals("masampat", (result as TranslationResult.Success).response.translatedText)
    }

    @Test
    fun connectionFailureReturnsFailure() = runBlocking {
        val api = object : TranslationApi {
            override suspend fun translate(request: TranslationRequest): TranslationResponse =
                throw IOException("offline")
        }

        val result = NetworkTranslationRepository(api).translate("English", "Bulos", "Beautiful")

        assertTrue(result is TranslationResult.Failure)
    }

    @Test
    fun generatedInstallationIdIsValidPersistedAndSentOnEveryRequest() {
        val store = MemoryInstallationIdStore()
        val provider = InstallationIdProvider(store)
        val firstId = provider.get()
        val secondId = provider.get()
        UUID.fromString(firstId)
        assertEquals(firstId, secondId)
        assertNotEquals("00000000-0000-0000-0000-000000000000", firstId)

        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("ok"))
            server.enqueue(MockResponse().setBody("ok"))
            val client = OkHttpClient.Builder().addInterceptor(DeviceIdInterceptor(provider)).build()
            repeat(2) {
                client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
            }
            assertEquals(firstId, server.takeRequest().getHeader("X-Device-ID"))
            assertEquals(firstId, server.takeRequest().getHeader("X-Device-ID"))
        }
    }

    private class RecordingApi(private val response: TranslationResponse) : TranslationApi {
        var lastRequest: TranslationRequest? = null

        override suspend fun translate(request: TranslationRequest): TranslationResponse {
            lastRequest = request
            return response
        }
    }

    private class MemoryInstallationIdStore : InstallationIdStore {
        private var value: String? = null
        override fun read(): String? = value
        override fun write(value: String) {
            this.value = value
        }
    }
}
