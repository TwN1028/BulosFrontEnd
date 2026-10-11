package com.example.bulosfrontend

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class InstallationIdPersistenceTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences
        get() = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Before
    fun clearIdentity() {
        preferences.edit().clear().commit()
    }

    @After
    fun cleanUpIdentity() {
        preferences.edit().clear().commit()
    }

    @Test
    fun validIdSurvivesProviderRecreationAndIsReusedInRequestHeader() {
        val first = InstallationIdProvider(context).get()
        val recreatedProvider = InstallationIdProvider(context)

        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("ok"))
            val client = OkHttpClient.Builder()
                .addInterceptor(DeviceIdInterceptor(recreatedProvider))
                .build()
            client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
            assertEquals(first, server.takeRequest().getHeader("X-Device-ID"))
        }
        assertEquals(first, preferences.getString(PREFERENCE_KEY, null))
    }

    @Test
    fun invalidAndNonV4StoredIdsAreReplacedWithV4Ids() {
        listOf("invalid", "00000000-0000-1000-8000-000000000000").forEach { stored ->
            preferences.edit().putString(PREFERENCE_KEY, stored).commit()
            val replacement = InstallationIdProvider(context).get()

            assertNotEquals(stored, replacement)
            assertEquals(4, UUID.fromString(replacement).version())
            assertEquals(replacement, preferences.getString(PREFERENCE_KEY, null))
        }
    }

    @Test
    fun saveFailureKeepsIdStableAndRetriesWithinProviderLifetime() {
        val store = FailingStore(failures = 1)
        val provider = InstallationIdProvider(store)

        val first = provider.get()
        val second = provider.get()

        assertEquals(first, second)
        assertEquals(first, store.read())
        assertEquals(2, store.writeAttempts)
    }

    private class FailingStore(private var failures: Int) : InstallationIdStore {
        private var value: String? = null
        var writeAttempts = 0

        override fun read(): String? = value

        override fun write(value: String): Boolean {
            writeAttempts++
            if (failures-- > 0) return false
            this.value = value
            return true
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "installation_identity"
        const val PREFERENCE_KEY = "installation_uuid"
    }
}
