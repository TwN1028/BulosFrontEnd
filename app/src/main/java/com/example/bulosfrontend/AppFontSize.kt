package com.example.bulosfrontend

enum class AppFontSize(val scaleFactor: Float) {
    SMALL(0.90f),
    MEDIUM(1.00f),
    LARGE(1.15f);

    companion object {
        fun fromStoredValue(value: String?): AppFontSize =
            entries.firstOrNull { it.name == value } ?: MEDIUM
    }
}
