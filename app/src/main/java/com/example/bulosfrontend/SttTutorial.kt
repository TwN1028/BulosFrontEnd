package com.example.bulosfrontend

/** Steps are driven only by real STT and translation outcomes. */
enum class SttTutorialStep {
    LANGUAGE,
    MICROPHONE,
    LISTENING,
    TRANSCRIPT,
    EDIT,
    TRANSLATE,
    COMPLETE,
}
