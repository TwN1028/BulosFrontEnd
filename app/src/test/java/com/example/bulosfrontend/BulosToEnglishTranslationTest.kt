package com.example.bulosfrontend

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BulosToEnglishTranslationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: TranslationApi
    private lateinit var networkRepository: NetworkTranslationRepository

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(TranslationApi::class.java)
        networkRepository = NetworkTranslationRepository(api)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `bulos to english sends correct bul to en request and parses final english output`() = runBlocking {
        val jsonResponseBody = """
            {
                "original_text": "Anu i ngalan mu?",
                "translated_text": "What is your name?",
                "source_language": "bul",
                "target_language": "en",
                "confidence": 0.98,
                "intermediate_language": "tl",
                "translation_method": "bul_en_pipeline"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponseBody)
        )

        val result = networkRepository.translate("Bulos", "English", "Anu i ngalan mu?")
        assertTrue(result is TranslationResult.Success)

        val success = result as TranslationResult.Success
        assertEquals("What is your name?", success.response.translatedText)
        assertEquals("bul", success.response.sourceLanguage)
        assertEquals("en", success.response.targetLanguage)

        // Verify request payload
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/translate/", recordedRequest.path)
        val requestBody = recordedRequest.body.readUtf8()
        assertTrue(requestBody.contains(""""source_language":"bul""""))
        assertTrue(requestBody.contains(""""target_language":"en""""))
        assertTrue(requestBody.contains(""""text":"Anu i ngalan mu?""""))
    }

    @Test
    fun `bulos to english representative inputs 1 to 3 parse final english outputs`() = runBlocking {
        // Representative Input 1: "Anu i ngalan mu?"
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"original_text":"Anu i ngalan mu?","translated_text":"What is your name?","source_language":"bul","target_language":"en","confidence":0.98,"intermediate_language":"tl","translation_method":"bul_en_pipeline"}"""
            )
        )
        val r1 = networkRepository.translate("Bulos", "English", "Anu i ngalan mu?") as TranslationResult.Success
        assertEquals("What is your name?", r1.response.translatedText)

        // Representative Input 2: "Hanga i beloy ni patod"
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"original_text":"Hanga i beloy ni patod","translated_text":"The house of man is big","source_language":"bul","target_language":"en","confidence":0.95,"intermediate_language":"tl","translation_method":"bul_en_pipeline"}"""
            )
        )
        val r2 = networkRepository.translate("Bulos", "English", "Hanga i beloy ni patod") as TranslationResult.Success
        assertEquals("The house of man is big", r2.response.translatedText)

        // Representative Input 3: "Binoy na un bokan."
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"original_text":"Binoy na un bokan.","translated_text":"He/She gave the flower","source_language":"bul","target_language":"en","confidence":0.95,"intermediate_language":"tl","translation_method":"bul_en_pipeline"}"""
            )
        )
        val r3 = networkRepository.translate("Bulos", "English", "Binoy na un bokan.") as TranslationResult.Success
        assertEquals("He/She gave the flower", r3.response.translatedText)
    }

    @Test
    fun `bulos to filipino sends bul to tl and returns filipino text`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"original_text":"Anu i ngalan mu?","translated_text":"Ano ang pangalan mo?","source_language":"bul","target_language":"tl","confidence":0.98,"intermediate_language":null,"translation_method":"bul_tl_direct"}"""
            )
        )

        val result = networkRepository.translate("Bulos", "Filipino", "Anu i ngalan mu?") as TranslationResult.Success
        assertEquals("Ano ang pangalan mo?", result.response.translatedText)

        val recordedRequest = mockWebServer.takeRequest()
        val requestBody = recordedRequest.body.readUtf8()
        assertTrue(requestBody.contains(""""source_language":"bul""""))
        assertTrue(requestBody.contains(""""target_language":"tl""""))
    }

    @Test
    fun `filipino to bulos and english to bulos send correct language codes`() = runBlocking {
        // Filipino -> Bulos: tl -> bul
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"original_text":"Ano ang pangalan mo?","translated_text":"Anu i ngalan mu?","source_language":"tl","target_language":"bul","confidence":0.98,"intermediate_language":null,"translation_method":"tl_bul_direct"}"""
            )
        )
        val r1 = networkRepository.translate("Filipino", "Bulos", "Ano ang pangalan mo?") as TranslationResult.Success
        assertEquals("Anu i ngalan mu?", r1.response.translatedText)
        val req1 = mockWebServer.takeRequest().body.readUtf8()
        assertTrue(req1.contains(""""source_language":"tl""""))
        assertTrue(req1.contains(""""target_language":"bul""""))

        // English -> Bulos: en -> bul
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"original_text":"What is your name?","translated_text":"Anu i ngalan mu?","source_language":"en","target_language":"bul","confidence":0.98,"intermediate_language":"tl","translation_method":"en_bul_pipeline"}"""
            )
        )
        val r2 = networkRepository.translate("English", "Bulos", "What is your name?") as TranslationResult.Success
        assertEquals("Anu i ngalan mu?", r2.response.translatedText)
        val req2 = mockWebServer.takeRequest().body.readUtf8()
        assertTrue(req2.contains(""""source_language":"en""""))
        assertTrue(req2.contains(""""target_language":"bul""""))
    }

    @Test
    fun `backend server errors and timeouts return failure result`() = runBlocking {
        // Test HTTP 500
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))
        val r1 = networkRepository.translate("Bulos", "English", "Anu i ngalan mu?")
        assertTrue(r1 is TranslationResult.Failure)

        // Test HTTP 400
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))
        val r2 = networkRepository.translate("Bulos", "English", "Anu i ngalan mu?")
        assertTrue(r2 is TranslationResult.Failure)
    }
}
