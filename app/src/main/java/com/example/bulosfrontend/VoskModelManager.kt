package com.example.bulosfrontend

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.vosk.Model
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/** Installs official Vosk models once and reuses them for fully offline recognition. */
class VoskModelManager(
    context: Context,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val modelRoot = File(context.filesDir, "vosk_models")

    fun supports(language: String): Boolean = modelSpec(language) != null

    fun load(language: String): Model {
        val spec = modelSpec(language)
            ?: throw IllegalArgumentException("No offline speech model is available for $language.")
        val installedDirectory = File(modelRoot, spec.name)
        val completionMarker = File(installedDirectory, COMPLETION_MARKER)
        if (!completionMarker.isFile) {
            install(spec, installedDirectory)
        }
        return Model(installedDirectory.absolutePath)
    }

    private fun install(spec: ModelSpec, installedDirectory: File) {
        modelRoot.mkdirs()
        val archive = File(modelRoot, "${spec.name}.zip.part")
        val stagingDirectory = File(modelRoot, "${spec.name}.installing")
        archive.delete()
        stagingDirectory.deleteRecursively()
        stagingDirectory.mkdirs()

        try {
            download(spec, archive)
            extract(spec, archive, stagingDirectory)
            File(stagingDirectory, COMPLETION_MARKER).writeText(spec.md5)
            installedDirectory.deleteRecursively()
            if (!stagingDirectory.renameTo(installedDirectory)) {
                throw IOException("Could not finish installing the offline speech model.")
            }
        } catch (error: Exception) {
            stagingDirectory.deleteRecursively()
            throw error
        } finally {
            archive.delete()
        }
    }

    private fun download(spec: ModelSpec, destination: File) {
        val request = Request.Builder().url(spec.url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Speech model download failed (${response.code}).")
            }
            val body = response.body ?: throw IOException("Speech model download was empty.")
            val digest = MessageDigest.getInstance("MD5")
            var downloadedBytes = 0L
            body.byteStream().use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        downloadedBytes += count
                    }
                }
            }
            val actualMd5 = digest.digest().joinToString("") { "%02x".format(it) }
            if (downloadedBytes != spec.size || !actualMd5.equals(spec.md5, ignoreCase = true)) {
                throw IOException("Speech model verification failed. Please try again.")
            }
        }
    }

    private fun extract(spec: ModelSpec, archive: File, destination: File) {
        val destinationPath = destination.canonicalPath + File.separator
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val normalizedName = entry.name.replace('\\', '/')
                val relativeName = normalizedName.removePrefix("${spec.name}/")
                if (relativeName.isEmpty()) continue
                val output = File(destination, relativeName)
                if (!output.canonicalPath.startsWith(destinationPath)) {
                    throw IOException("Invalid path in speech model archive.")
                }
                if (entry.isDirectory) {
                    output.mkdirs()
                } else {
                    output.parentFile?.mkdirs()
                    output.outputStream().buffered().use { zip.copyTo(it) }
                }
                zip.closeEntry()
            }
        }
        if (!File(destination, "conf").isDirectory || !File(destination, "am").isDirectory) {
            throw IOException("The downloaded speech model is incomplete.")
        }
    }

    private fun modelSpec(language: String): ModelSpec? = when {
        language.equals("English", ignoreCase = true) -> ENGLISH_MODEL
        language.equals("Filipino", ignoreCase = true) -> FILIPINO_MODEL
        else -> null
    }

    private data class ModelSpec(
        val name: String,
        val url: String,
        val md5: String,
        val size: Long,
    )

    private companion object {
        const val COMPLETION_MARKER = ".installed"

        val ENGLISH_MODEL = ModelSpec(
            name = "vosk-model-small-en-us-0.15",
            url = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
            md5 = "09ab50ccd62b674cbaa231b825f9c1cb",
            size = 41_205_931L,
        )

        val FILIPINO_MODEL = ModelSpec(
            name = "vosk-model-tl-ph-generic-0.6",
            url = "https://alphacephei.com/vosk/models/vosk-model-tl-ph-generic-0.6.zip",
            md5 = "90b3dd8115dabdb4845972c2eb22797b",
            size = 329_169_068L,
        )
    }
}
