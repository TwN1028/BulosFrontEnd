package com.example.bulosfrontend

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.Executors

class TranslationIntegrationTest {

    @Test
    fun partialLegacyEnglishToBulosResponseCanUseTheLocalFilipinoBridge() {
        val response = TranslationResponse(
            originalText = "house",
            translatedText = "house",
            sourceLanguage = "en",
            targetLanguage = "bul",
            confidence = 0.0,
            intermediateLanguage = null,
            translationMethod = "word_match_partial",
        )

        assertTrue(shouldRepairPartialEnglishBulosResponse("English", "Bulos", response))
        assertTrue(!shouldRepairPartialEnglishBulosResponse("English", "Filipino", response))
        assertTrue(
            !shouldRepairPartialEnglishBulosResponse(
                "English",
                "Bulos",
                response.copy(confidence = 1.0),
            ),
        )
        assertTrue(
            !shouldRepairPartialEnglishBulosResponse(
                "English",
                "Bulos",
                response.copy(translationMethod = "en_bul_filipino_fallback"),
            ),
        )
    }
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
    fun bulosToEnglishSendsCorrectRequestAndReturnsFinalBackendTextForRepresentativeInputs() = runBlocking {
        val examples = listOf(
            "Denu?" to "Where?",
            "Kapide ka dumalong?" to "When did you come?",
            "Hanga i beloy ni patod" to "The man's house is big",
        )

        MockWebServer().use { server ->
            examples.forEach { (input, english) ->
                server.enqueue(
                    MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody(
                            Gson().toJson(
                                mapOf(
                                    "original_text" to input,
                                    "translated_text" to english,
                                    "source_language" to "bul",
                                    "target_language" to "en",
                                    "confidence" to 0.9,
                                    "intermediate_language" to "tl",
                                    "translation_method" to "bul_filipino_english",
                                ),
                            ),
                        ),
                )
            }
            val api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TranslationApi::class.java)
            val repository = NetworkTranslationRepository(api)

            examples.forEach { (input, expectedEnglish) ->
                val result = repository.translate("Bulos", "English", input)
                assertTrue(result is TranslationResult.Success)
                assertEquals(
                    expectedEnglish,
                    (result as TranslationResult.Success).response.translatedText,
                )

                val request = server.takeRequest()
                assertEquals("POST", request.method)
                assertEquals("/api/v1/translate/", request.path)
                val body = Gson().fromJson(request.body.readUtf8(), JsonObject::class.java)
                assertEquals("bul", body.get("source_language").asString)
                assertEquals("en", body.get("target_language").asString)
                assertEquals(input, body.get("text").asString)
            }
        }
    }

    @Test
    fun bulosToFilipinoStillUsesTheFilipinoBackendResponse() = runBlocking {
        val api = RecordingApi(
            TranslationResponse(
                originalText = "Denu?",
                translatedText = "Saan?",
                sourceLanguage = "bul",
                targetLanguage = "tl",
                confidence = 1.0,
                intermediateLanguage = null,
                translationMethod = "dictionary",
            ),
        )

        val result = NetworkTranslationRepository(api).translate("Bulos", "Filipino", "Denu?")

        assertEquals("bul", api.lastRequest?.sourceLanguage)
        assertEquals("tl", api.lastRequest?.targetLanguage)
        assertEquals("Saan?", (result as TranslationResult.Success).response.translatedText)
    }

    @Test
    fun bulosToEnglishTimeoutAndBlankOutputAreFailures() = runBlocking {
        val timeoutApi = object : TranslationApi {
            override suspend fun translate(request: TranslationRequest): TranslationResponse =
                throw SocketTimeoutException("backend timeout")
        }
        val timeoutResult = NetworkTranslationRepository(timeoutApi)
            .translate("Bulos", "English", "Denu?")
        assertTrue(timeoutResult is TranslationResult.Failure)
        assertTrue((timeoutResult as TranslationResult.Failure).message.contains("too long"))

        val blankResponseApi = RecordingApi(
            TranslationResponse(
                originalText = "Denu?",
                translatedText = "   ",
                sourceLanguage = "bul",
                targetLanguage = "en",
                confidence = 0.0,
                intermediateLanguage = "tl",
                translationMethod = "bul_filipino_english",
            ),
        )
        assertTrue(
            NetworkTranslationRepository(blankResponseApi).translate("Bulos", "English", "Denu?")
                is TranslationResult.Failure,
        )
    }

    @Test
    fun missingEnglishResponseFieldIsNotAcceptedAsSuccess() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(
                        """{"original_text":"Denu?","source_language":"bul","target_language":"en","confidence":0.0,"intermediate_language":"tl","translation_method":"bul_filipino_english"}""",
                    ),
            )
            val api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TranslationApi::class.java)

            val result = NetworkTranslationRepository(api).translate("Bulos", "English", "Denu?")

            assertTrue(result is TranslationResult.Failure)
        }
    }

    @Test
    fun bulosToEnglishDoesNotUseOfflineResultAfterBackendFailureOrOfflineMode() {
        val backendFailure = TranslationResult.Failure("Translation service is unavailable.")
        var offlineFallbackCalled = false
        val onlineFailure = applyOfflineFallback(
            source = UiLanguage.BULOS,
            target = UiLanguage.ENGLISH,
            networkResult = backendFailure,
            wasOnline = true,
        ) {
            offlineFallbackCalled = true
            TranslationResult.Success(
                TranslationResponse("Denu?", "Saan?", "bul", "en", 1.0, null, "offline"),
            )
        }
        assertEquals(backendFailure, onlineFailure)
        assertFalse(offlineFallbackCalled)

        val offlineModeFailure = applyOfflineFallback(
            source = UiLanguage.BULOS,
            target = UiLanguage.ENGLISH,
            networkResult = TranslationResult.Failure("Offline"),
            wasOnline = false,
        ) {
            offlineFallbackCalled = true
            backendFailure
        }
        assertEquals(
            TranslationResult.Failure(BULOS_TO_ENGLISH_ONLINE_REQUIRED_MESSAGE),
            offlineModeFailure,
        )
        assertFalse(offlineFallbackCalled)
    }

    @Test
    fun otherOfflineTranslationDirectionsStillUseTheirFallback() {
        val expected = TranslationResult.Failure("offline fallback")
        listOf(
            UiLanguage.FILIPINO to UiLanguage.BULOS,
            UiLanguage.ENGLISH to UiLanguage.BULOS,
            UiLanguage.BULOS to UiLanguage.FILIPINO,
        ).forEach { (source, target) ->
            var fallbackCalled = false
            val result = applyOfflineFallback(
                source = source,
                target = target,
                networkResult = TranslationResult.Failure("Offline"),
                wasOnline = false,
            ) {
                fallbackCalled = true
                expected
            }
            assertTrue(fallbackCalled)
            assertEquals(expected, result)
        }
    }

    @Test
    fun displayedResponseMustMatchCurrentTextAndLanguagePair() {
        val response = TranslationResponse(
            originalText = "Denu?",
            translatedText = "Where?",
            sourceLanguage = "bul",
            targetLanguage = "en",
            confidence = 0.9,
            intermediateLanguage = "tl",
            translationMethod = "bul_filipino_english",
        )

        assertTrue(response.matchesTranslationRequest("Denu?", "Bulos", "English"))
        assertFalse(response.matchesTranslationRequest("Denu?", "Bulos", "Filipino"))
        assertFalse(response.matchesTranslationRequest("Denu?", "English", "Bulos"))
        assertFalse(response.matchesTranslationRequest("Kapide?", "Bulos", "English"))
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
    fun serverTimeoutIsNotReportedAsNoInternet() = runBlocking {
        val api = object : TranslationApi {
            override suspend fun translate(request: TranslationRequest): TranslationResponse =
                throw SocketTimeoutException("server timeout")
        }

        val result = NetworkTranslationRepository(api).translate("English", "Bulos", "Hello")

        assertTrue(result is TranslationResult.Failure)
        val message = (result as TranslationResult.Failure).message
        assertTrue(message.contains("server", ignoreCase = true))
        assertTrue(!message.contains("internet", ignoreCase = true))
    }

    @Test
    fun identicalLanguagePairIsRejectedBeforeApiSubmission() = runBlocking {
        val api = RecordingApi(
            TranslationResponse(
                originalText = "Hello",
                translatedText = "Hello",
                sourceLanguage = "en",
                targetLanguage = "en",
                confidence = 1.0,
                intermediateLanguage = null,
                translationMethod = "identity",
            ),
        )

        val result = NetworkTranslationRepository(api).translate("English", "English", "Hello")

        assertTrue(result is TranslationResult.Failure)
        assertEquals(null, api.lastRequest)
    }

    @Test
    fun unsupportedNonIdenticalPairIsRejectedBeforeApiSubmission() = runBlocking {
        val api = RecordingApi(
            TranslationResponse("Hello", "Kamusta", "en", "tl", 1.0, null, "dictionary"),
        )

        val result = NetworkTranslationRepository(api).translate("English", "Filipino", "Hello")

        assertTrue(result is TranslationResult.Failure)
        assertEquals(null, api.lastRequest)
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

    @Test
    fun validV4IsKeptButInvalidAndNonV4IdsAreReplaced() {
        val valid = UUID.randomUUID().toString()
        assertEquals(valid, InstallationIdProvider(MemoryInstallationIdStore(valid)).get())

        listOf("not-a-uuid", "00000000-0000-1000-8000-000000000000").forEach { stored ->
            val replacement = InstallationIdProvider(MemoryInstallationIdStore(stored)).get()
            assertNotEquals(stored, replacement)
            assertEquals(4, UUID.fromString(replacement).version())
        }
    }

    @Test
    fun failedSaveKeepsOneInMemoryIdAndRetriesPersistence() {
        val store = MemoryInstallationIdStore(writeFailuresRemaining = 1)
        val provider = InstallationIdProvider(store)

        val first = provider.get()
        val second = provider.get()

        assertEquals(first, second)
        assertEquals(first, store.read())
        assertEquals(2, store.writeAttempts)
    }

    @Test
    fun concurrentRequestsShareOneGeneratedId() {
        val provider = InstallationIdProvider(MemoryInstallationIdStore())
        val executor = Executors.newFixedThreadPool(8)
        try {
            val ids = (1..32).map { executor.submit<String> { provider.get() } }
                .map { it.get() }
            assertEquals(1, ids.toSet().size)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun onlyDeviceSpecificBadRequestsGetActionableMessage() = runBlocking {
        val deviceError = failingRepository(400, "{\"detail\":\"X-Device-ID is missing or invalid\"}")
            .translate("English", "Bulos", "Hello") as TranslationResult.Failure
        val otherBadRequest = failingRepository(400, "{\"detail\":\"Text is required\"}")
            .translate("English", "Bulos", "Hello") as TranslationResult.Failure

        assertTrue(deviceError.message.contains("Restart the app"))
        assertTrue(!deviceError.message.contains("X-Device-ID"))
        assertEquals("Translation service is unavailable. Please try again.", otherBadRequest.message)
    }

    @Test
    fun onlineFailureIsPreservedWhenOfflineFallbackCannotAnswer() {
        val timeout = TranslationResult.Failure("The translation server took too long to respond.")

        assertEquals(timeout, unresolvedFallback(timeout, wasOnline = true, hasOfflineData = false))
        assertEquals(timeout, unresolvedFallback(timeout, wasOnline = true, hasOfflineData = true))
    }

    @Test
    fun offlineFallbackExplainsMissingOrUnmatchedLocalData() {
        val offline = TranslationResult.Failure("Unable to connect.")

        assertEquals(
            TranslationResult.Failure(
                "Offline translation data is not installed. Connect to the internet once to download it.",
            ),
            unresolvedFallback(offline, wasOnline = false, hasOfflineData = false),
        )
        assertEquals(
            TranslationResult.Failure("Translation unavailable."),
            unresolvedFallback(offline, wasOnline = false, hasOfflineData = true),
        )
    }

    private fun failingRepository(code: Int, body: String): NetworkTranslationRepository {
        val api = object : TranslationApi {
            override suspend fun translate(request: TranslationRequest): TranslationResponse {
                throw HttpException(Response.error<TranslationResponse>(code, body.toResponseBody()))
            }
        }
        return NetworkTranslationRepository(api)
    }

    private class RecordingApi(private val response: TranslationResponse) : TranslationApi {
        var lastRequest: TranslationRequest? = null

        override suspend fun translate(request: TranslationRequest): TranslationResponse {
            lastRequest = request
            return response
        }
    }

    private class MemoryInstallationIdStore(
        private var value: String? = null,
        private var writeFailuresRemaining: Int = 0,
    ) : InstallationIdStore {
        var writeAttempts = 0
        override fun read(): String? = value
        override fun write(value: String): Boolean {
            writeAttempts++
            if (writeFailuresRemaining > 0) {
                writeFailuresRemaining--
                return false
            }
            this.value = value
            return true
        }
    }
}
