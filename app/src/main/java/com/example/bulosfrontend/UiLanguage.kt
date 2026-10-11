package com.example.bulosfrontend

import androidx.annotation.StringRes

enum class UiLanguage {
    ENGLISH,
    FILIPINO,
    BULOS;

    @get:StringRes
    val displayNameRes: Int
        get() = when (this) {
            ENGLISH -> R.string.language_name_english
            FILIPINO -> R.string.language_name_filipino
            BULOS -> R.string.language_name_bulos
        }

    companion object {
        fun fromStoredValue(value: String?): UiLanguage? = entries.firstOrNull { it.name == value }
    }
}
