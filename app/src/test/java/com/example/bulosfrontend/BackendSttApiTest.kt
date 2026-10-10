package com.example.bulosfrontend

import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException

class BackendSttApiTest {
    @Test
    fun `client posts audio and language as multipart fields and keeps device id`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(successResponse))
            val deviceId = UUID.randomUUID().toString()
            val client = OkHttpClient.Builder()
                .addInterceptor(DeviceIdInterceptor(InstallationIdProvider(FixedInstallationIdStore(deviceId))))
                .build()
            val api = BackendSttClientFactory.create(server.url("/").toString(), client)

            val response = api.transcribe(
                audio = BackendSttClientFactory.audioPart(wavFixture(), "audio/wav"),
                language = BackendSttClientFactory.languagePart("bul"),
            )

            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/stt/transcribe", request.path)
            assertEquals(deviceId, request.getHeader("X-Device-ID"))
            assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
            val multipartBody = request.body.readUtf8()
            assertTrue(multipartBody.contains("name=\"audio\""))
            assertTrue(multipartBody.contains("name=\"language\""))
            assertEquals("hello", response.text)
            assertEquals("bul", response.language)
            assertEquals(0.98, response.languageProbability, 0.0)
            assertEquals(1, response.segments.size)
        }
    }

    @Test
    fun `success response supports published optional error field`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(successResponse))
            val api = BackendSttClientFactory.create(server.url("/").toString(), OkHttpClient())

            val response = api.transcribe(BackendSttClientFactory.audioPart(wavFixture(), "audio/wav"))

            assertEquals(1.25, response.duration, 0.0)
            assertEquals(null, response.error)
            assertNotNull(response.segments.first())
        }
    }

    @Test
    fun `empty files and unsupported language are rejected before upload`() {
        val empty = File.createTempFile("bulos-empty", ".wav").apply { deleteOnExit() }
        assertTrue(runCatching { BackendSttClientFactory.audioPart(empty, "audio/wav") }.isFailure)
        assertTrue(runCatching { BackendSttClientFactory.languagePart("fil") }.isFailure)
    }

    @Test
    fun `http errors surface as retrofit errors without response parsing crashes`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(400).setBody("{\"error\":\"Empty audio file\"}"))
            val api = BackendSttClientFactory.create(server.url("/").toString(), OkHttpClient())

            val failure = runCatching {
                api.transcribe(BackendSttClientFactory.audioPart(wavFixture(), "audio/wav"))
            }
                .exceptionOrNull()

            assertTrue(failure is HttpException)
            assertEquals(400, (failure as HttpException).code())
        }
    }

    private fun wavFixture(): File = File.createTempFile("bulos-stt", ".wav").apply {
        writeBytes(byteArrayOf(0x52, 0x49, 0x46, 0x46)) // Minimal non-sensitive WAV fixture header.
        deleteOnExit()
    }

    private class FixedInstallationIdStore(private val value: String) : InstallationIdStore {
        override fun read(): String = value
        override fun write(value: String): Boolean = true
    }

    private companion object {
        const val successResponse = """{
            "text":"hello",
            "segments":[{"id":0,"text":"hello"}],
            "language":"bul",
            "language_probability":0.98,
            "duration":1.25,
            "error":null
        }"""
    }
}
