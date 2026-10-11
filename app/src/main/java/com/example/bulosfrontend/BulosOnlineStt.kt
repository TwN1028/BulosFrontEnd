package com.example.bulosfrontend

/** Pure routing and response validation for the opt-in Bulos-only online STT path. */
internal object BulosOnlineStt {
    fun shouldUseBackend(sourceLanguage: String, canUseOnlineServices: Boolean): Boolean =
        sourceLanguage.equals("Bulos", ignoreCase = true) && canUseOnlineServices

    fun recognizedText(response: BackendSttTranscriptionResponse): String {
        require(response.error.isNullOrBlank()) { response.error ?: "Backend transcription failed." }
        return TextNormalizer.cleanForDisplay(response.text)
            .takeIf(String::isNotBlank)
            ?: throw IllegalStateException("Backend transcription returned no recognized text.")
    }
}
