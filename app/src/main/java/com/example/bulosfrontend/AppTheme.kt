package com.example.bulosfrontend

enum class AppTheme {
    LIGHT,
    DARK;

    companion object {
        fun fromStoredValue(value: String?): AppTheme =
            entries.firstOrNull { it.name == value } ?: LIGHT
    }
}
