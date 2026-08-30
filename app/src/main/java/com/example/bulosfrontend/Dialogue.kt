package com.example.bulosfrontend

import androidx.annotation.StringRes

data class DialogueContent(
    @StringRes val mainHeaderRes: Int,
    @StringRes val translateHeaderRes: Int,
    @StringRes val resultHeaderRes: Int,
    @StringRes val voiceHeaderRes: Int,
    @StringRes val translateActionRes: Int,
    @StringRes val placeholderRes: Int,
    @StringRes val backToHomeRes: Int,
    @StringRes val copiedRes: Int,
    @StringRes val recordStartLabelRes: Int,
    @StringRes val cancelBtnRes: Int,
    @StringRes val goBackRes: Int,
    @StringRes val historyHeaderRes: Int,
    @StringRes val footerRes: Int,
    @StringRes val stopRes: Int,
    @StringRes val copyDescriptionRes: Int,
    @StringRes val resultPlaceholderRes: Int,
    @StringRes val noHistoryRes: Int,
    @StringRes val voiceResultRes: Int,
    @StringRes val voiceRecordingRes: Int,
    @StringRes val characterCountRes: Int,
    val home: HomeDialogueContent,
    val speechResult: SpeechResultDialogueContent,
    val textTranslation: TextTranslationDialogueContent,
    val preservationIntro: PreservationIntroDialogueContent,
)

data class PreservationIntroDialogueContent(
    @StringRes val titleRes: Int,
    @StringRes val supportRes: Int,
    @StringRes val firstParagraphRes: Int,
    @StringRes val secondParagraphRes: Int,
    @StringRes val thirdParagraphRes: Int,
    @StringRes val finalMessageRes: Int,
    @StringRes val continueRes: Int,
    @StringRes val iconDescriptionRes: Int,
    @StringRes val indigenousLanguageRes: Int,
    @StringRes val preservationRes: Int,
    @StringRes val digitalAccessRes: Int,
    @StringRes val finalLeadRes: Int,
    @StringRes val finalDetailRes: Int,
)

data class TextTranslationDialogueContent(
    @StringRes val subtitleRes: Int,
    @StringRes val inputPlaceholderRes: Int,
    @StringRes val translateToRes: Int,
)

data class SpeechResultDialogueContent(
    @StringRes val speechSubtitleRes: Int,
    @StringRes val idleStatusRes: Int,
    @StringRes val listeningStatusRes: Int,
    @StringRes val reviewStatusRes: Int,
    @StringRes val transcribingRes: Int,
    @StringRes val recognizedTextLabelRes: Int,
    @StringRes val recognizedTextPlaceholderRes: Int,
    @StringRes val editRes: Int,
    @StringRes val doneEditingRes: Int,
    @StringRes val translateRes: Int,
    @StringRes val clearRes: Int,
    @StringRes val originalRes: Int,
    @StringRes val translationRes: Int,
    @StringRes val copyRes: Int,
    @StringRes val shareRes: Int,
    @StringRes val translateAgainRes: Int,
    @StringRes val swapLanguagesDescriptionRes: Int,
    @StringRes val microphoneDescriptionRes: Int,
    @StringRes val shareChooserTitleRes: Int,
    @StringRes val shareBodyRes: Int,
)

data class HomeDialogueContent(
    @StringRes val selectionTitleRes: Int,
    @StringRes val speechTitleRes: Int, @StringRes val speechSubtitleRes: Int,
    @StringRes val textTitleRes: Int, @StringRes val textSubtitleRes: Int,
    @StringRes val dictionaryTitleRes: Int, @StringRes val dictionarySubtitleRes: Int,
    @StringRes val historyTitleRes: Int, @StringRes val historySubtitleRes: Int,
    @StringRes val recentTranslationsRes: Int, @StringRes val seeAllRes: Int, @StringRes val noRecentRes: Int,
    @StringRes val navHomeRes: Int, @StringRes val navTranslateRes: Int, @StringRes val navSpeechRes: Int,
    @StringRes val navDictionaryRes: Int, @StringRes val navMoreRes: Int,
    @StringRes val dictionaryMessageRes: Int, @StringRes val placeholderDescriptionRes: Int,
    @StringRes val settingsTitleRes: Int, @StringRes val languageRes: Int,
    @StringRes val languageSummaryRes: Int, @StringRes val languageScreenTitleRes: Int,
    @StringRes val currentLanguageRes: Int, @StringRes val appLogoDescriptionRes: Int,
    @StringRes val homeBadgeRes: Int, @StringRes val settingsSubtitleRes: Int,
    @StringRes val supportedLanguagesRes: Int, @StringRes val supportedLanguagesDescriptionRes: Int,
    @StringRes val recentDynamicTitleRes: Int, @StringRes val viewHistoryRes: Int,
    @StringRes val dictionarySearchRes: Int, @StringRes val dictionarySearchDescriptionRes: Int,
    @StringRes val dictionaryEmptyRes: Int, @StringRes val dictionaryNoResultsRes: Int,
    @StringRes val appearanceRes: Int, @StringRes val themeRes: Int,
    @StringRes val lightThemeRes: Int, @StringRes val darkThemeRes: Int,
)

object DialogueProvider {
    private val dialogues = mapOf(
        UiLanguage.ENGLISH to DialogueContent(
            R.string.ui_header_main_eng, R.string.ui_header_translate_eng, R.string.ui_header_result_eng,
            R.string.ui_header_voice_eng, R.string.ui_translate_action_eng, R.string.ui_input_placeholder_eng,
            R.string.ui_back_home_eng, R.string.ui_copied_eng, R.string.ui_record_start_eng,
            R.string.ui_cancel_eng, R.string.ui_go_back_eng, R.string.ui_header_history_eng,
            R.string.ui_footer_eng, R.string.ui_stop_eng, R.string.ui_copy_description_eng,
            R.string.ui_result_placeholder_eng, R.string.ui_no_history_eng, R.string.ui_voice_result_eng,
            R.string.ui_voice_recording_eng, R.string.ui_character_count_eng,
            home("eng"),
            speechResult("eng"),
            textTranslation("eng"),
            preservationIntro("eng"),
        ),
        UiLanguage.FILIPINO to DialogueContent(
            R.string.ui_header_main_fil, R.string.ui_header_translate_fil, R.string.ui_header_result_fil,
            R.string.ui_header_voice_fil, R.string.ui_translate_action_fil, R.string.ui_input_placeholder_fil,
            R.string.ui_back_home_fil, R.string.ui_copied_fil, R.string.ui_record_start_fil,
            R.string.ui_cancel_fil, R.string.ui_go_back_fil, R.string.ui_header_history_fil,
            R.string.ui_footer_fil, R.string.ui_stop_fil, R.string.ui_copy_description_fil,
            R.string.ui_result_placeholder_fil, R.string.ui_no_history_fil, R.string.ui_voice_result_fil,
            R.string.ui_voice_recording_fil, R.string.ui_character_count_fil,
            home("fil"),
            speechResult("fil"),
            textTranslation("fil"),
            preservationIntro("fil"),
        ),
        UiLanguage.BULOS to DialogueContent(
            R.string.ui_header_main_bul, R.string.ui_header_translate_bul, R.string.ui_header_result_bul,
            R.string.ui_header_voice_bul, R.string.ui_translate_action_bul, R.string.ui_input_placeholder_bul,
            R.string.ui_back_home_bul, R.string.ui_copied_bul, R.string.ui_record_start_bul,
            R.string.ui_cancel_bul, R.string.ui_go_back_bul, R.string.ui_header_history_bul,
            R.string.ui_footer_bul, R.string.ui_stop_bul, R.string.ui_copy_description_bul,
            R.string.ui_result_placeholder_bul, R.string.ui_no_history_bul, R.string.ui_voice_result_bul,
            R.string.ui_voice_recording_bul, R.string.ui_character_count_bul,
            home("bul"),
            speechResult("bul"),
            textTranslation("bul"),
            preservationIntro("bul"),
        ),
    )

    fun getDialogue(language: UiLanguage): DialogueContent = dialogues.getValue(language)

    private fun preservationIntro(suffix: String): PreservationIntroDialogueContent = when (suffix) {
        "fil" -> PreservationIntroDialogueContent(
            R.string.ui_preservation_title_fil,
            R.string.ui_preservation_support_fil,
            R.string.ui_preservation_paragraph_one_fil,
            R.string.ui_preservation_paragraph_two_fil,
            R.string.ui_preservation_paragraph_three_fil,
            R.string.ui_preservation_final_message_fil,
            R.string.ui_preservation_continue_fil,
            R.string.ui_preservation_icon_description_fil,
            R.string.ui_preservation_indigenous_language_fil,
            R.string.ui_preservation_concept_fil,
            R.string.ui_preservation_digital_access_fil,
            R.string.ui_preservation_final_lead_fil,
            R.string.ui_preservation_final_detail_fil,
        )
        "bul" -> PreservationIntroDialogueContent(
            R.string.ui_preservation_title_bul,
            R.string.ui_preservation_support_bul,
            R.string.ui_preservation_paragraph_one_bul,
            R.string.ui_preservation_paragraph_two_bul,
            R.string.ui_preservation_paragraph_three_bul,
            R.string.ui_preservation_final_message_bul,
            R.string.ui_preservation_continue_bul,
            R.string.ui_preservation_icon_description_bul,
            R.string.ui_preservation_indigenous_language_bul,
            R.string.ui_preservation_concept_bul,
            R.string.ui_preservation_digital_access_bul,
            R.string.ui_preservation_final_lead_bul,
            R.string.ui_preservation_final_detail_bul,
        )
        else -> PreservationIntroDialogueContent(
            R.string.ui_preservation_title_eng,
            R.string.ui_preservation_support_eng,
            R.string.ui_preservation_paragraph_one_eng,
            R.string.ui_preservation_paragraph_two_eng,
            R.string.ui_preservation_paragraph_three_eng,
            R.string.ui_preservation_final_message_eng,
            R.string.ui_preservation_continue_eng,
            R.string.ui_preservation_icon_description_eng,
            R.string.ui_preservation_indigenous_language_eng,
            R.string.ui_preservation_concept_eng,
            R.string.ui_preservation_digital_access_eng,
            R.string.ui_preservation_final_lead_eng,
            R.string.ui_preservation_final_detail_eng,
        )
    }

    private fun textTranslation(suffix: String): TextTranslationDialogueContent = when (suffix) {
        "fil" -> TextTranslationDialogueContent(
            R.string.ui_text_subtitle_fil,
            R.string.ui_text_input_placeholder_fil,
            R.string.ui_translate_to_fil,
        )
        "bul" -> TextTranslationDialogueContent(
            R.string.ui_text_subtitle_bul,
            R.string.ui_text_input_placeholder_bul,
            R.string.ui_translate_to_bul,
        )
        else -> TextTranslationDialogueContent(
            R.string.ui_text_subtitle_eng,
            R.string.ui_text_input_placeholder_eng,
            R.string.ui_translate_to_eng,
        )
    }

    private fun speechResult(suffix: String): SpeechResultDialogueContent = when (suffix) {
        "fil" -> SpeechResultDialogueContent(
            R.string.ui_speech_subtitle_fil, R.string.ui_speech_idle_fil,
            R.string.ui_speech_listening_fil, R.string.ui_speech_review_fil, R.string.ui_transcribing_fil,
            R.string.ui_recognized_text_label_fil, R.string.ui_recognized_text_placeholder_fil,
            R.string.ui_edit_fil, R.string.ui_done_editing_fil, R.string.ui_translate_voice_fil,
            R.string.ui_clear_fil, R.string.ui_original_fil, R.string.ui_translation_fil,
            R.string.ui_copy_fil, R.string.ui_share_fil, R.string.ui_translate_again_fil,
            R.string.ui_swap_languages_description_fil, R.string.ui_microphone_description_fil,
            R.string.ui_share_chooser_title_fil, R.string.ui_share_body_fil,
        )
        "bul" -> SpeechResultDialogueContent(
            R.string.ui_speech_subtitle_bul, R.string.ui_speech_idle_bul,
            R.string.ui_speech_listening_bul, R.string.ui_speech_review_bul, R.string.ui_transcribing_bul,
            R.string.ui_recognized_text_label_bul, R.string.ui_recognized_text_placeholder_bul,
            R.string.ui_edit_bul, R.string.ui_done_editing_bul, R.string.ui_translate_voice_bul,
            R.string.ui_clear_bul, R.string.ui_original_bul, R.string.ui_translation_bul,
            R.string.ui_copy_bul, R.string.ui_share_bul, R.string.ui_translate_again_bul,
            R.string.ui_swap_languages_description_bul, R.string.ui_microphone_description_bul,
            R.string.ui_share_chooser_title_bul, R.string.ui_share_body_bul,
        )
        else -> SpeechResultDialogueContent(
            R.string.ui_speech_subtitle_eng, R.string.ui_speech_idle_eng,
            R.string.ui_speech_listening_eng, R.string.ui_speech_review_eng, R.string.ui_transcribing_eng,
            R.string.ui_recognized_text_label_eng, R.string.ui_recognized_text_placeholder_eng,
            R.string.ui_edit_eng, R.string.ui_done_editing_eng, R.string.ui_translate_voice_eng,
            R.string.ui_clear_eng, R.string.ui_original_eng, R.string.ui_translation_eng,
            R.string.ui_copy_eng, R.string.ui_share_eng, R.string.ui_translate_again_eng,
            R.string.ui_swap_languages_description_eng, R.string.ui_microphone_description_eng,
            R.string.ui_share_chooser_title_eng, R.string.ui_share_body_eng,
        )
    }

    private fun home(suffix: String): HomeDialogueContent = when (suffix) {
        "fil" -> HomeDialogueContent(
            R.string.ui_selection_title_fil,
            R.string.ui_feature_speech_title_fil, R.string.ui_feature_speech_subtitle_fil,
            R.string.ui_feature_text_title_fil, R.string.ui_feature_text_subtitle_fil,
            R.string.ui_feature_dictionary_title_fil, R.string.ui_feature_dictionary_subtitle_fil,
            R.string.ui_feature_history_title_fil, R.string.ui_feature_history_subtitle_fil,
            R.string.ui_recent_fil, R.string.ui_see_all_fil, R.string.ui_no_recent_fil,
            R.string.ui_nav_home_fil, R.string.ui_nav_translate_fil, R.string.ui_nav_speech_fil,
            R.string.ui_nav_dictionary_fil, R.string.ui_nav_more_fil,
            R.string.ui_dictionary_message_fil, R.string.ui_placeholder_description_fil,
            R.string.ui_settings_title_fil, R.string.ui_language_fil, R.string.ui_language_summary_fil,
            R.string.ui_language_screen_title_fil, R.string.ui_current_language_fil,
            R.string.ui_app_logo_description_fil,
            R.string.ui_home_badge_fil, R.string.ui_settings_subtitle_fil,
            R.string.ui_supported_languages_fil, R.string.ui_supported_languages_description_fil,
            R.string.ui_recent_dynamic_title_fil, R.string.ui_view_history_fil,
            R.string.ui_dictionary_search_fil, R.string.ui_dictionary_search_description_fil,
            R.string.ui_dictionary_empty_fil, R.string.ui_dictionary_no_results_fil,
            R.string.ui_appearance_fil, R.string.ui_theme_fil,
            R.string.ui_theme_light_fil, R.string.ui_theme_dark_fil,
        )
        "bul" -> HomeDialogueContent(
            R.string.ui_selection_title_bul,
            R.string.ui_feature_speech_title_bul, R.string.ui_feature_speech_subtitle_bul,
            R.string.ui_feature_text_title_bul, R.string.ui_feature_text_subtitle_bul,
            R.string.ui_feature_dictionary_title_bul, R.string.ui_feature_dictionary_subtitle_bul,
            R.string.ui_feature_history_title_bul, R.string.ui_feature_history_subtitle_bul,
            R.string.ui_recent_bul, R.string.ui_see_all_bul, R.string.ui_no_recent_bul,
            R.string.ui_nav_home_bul, R.string.ui_nav_translate_bul, R.string.ui_nav_speech_bul,
            R.string.ui_nav_dictionary_bul, R.string.ui_nav_more_bul,
            R.string.ui_dictionary_message_bul, R.string.ui_placeholder_description_bul,
            R.string.ui_settings_title_bul, R.string.ui_language_bul, R.string.ui_language_summary_bul,
            R.string.ui_language_screen_title_bul, R.string.ui_current_language_bul,
            R.string.ui_app_logo_description_bul,
            R.string.ui_home_badge_bul, R.string.ui_settings_subtitle_bul,
            R.string.ui_supported_languages_bul, R.string.ui_supported_languages_description_bul,
            R.string.ui_recent_dynamic_title_bul, R.string.ui_view_history_bul,
            R.string.ui_dictionary_search_bul, R.string.ui_dictionary_search_description_bul,
            R.string.ui_dictionary_empty_bul, R.string.ui_dictionary_no_results_bul,
            R.string.ui_appearance_bul, R.string.ui_theme_bul,
            R.string.ui_theme_light_bul, R.string.ui_theme_dark_bul,
        )
        else -> HomeDialogueContent(
            R.string.ui_selection_title_eng,
            R.string.ui_feature_speech_title_eng, R.string.ui_feature_speech_subtitle_eng,
            R.string.ui_feature_text_title_eng, R.string.ui_feature_text_subtitle_eng,
            R.string.ui_feature_dictionary_title_eng, R.string.ui_feature_dictionary_subtitle_eng,
            R.string.ui_feature_history_title_eng, R.string.ui_feature_history_subtitle_eng,
            R.string.ui_recent_eng, R.string.ui_see_all_eng, R.string.ui_no_recent_eng,
            R.string.ui_nav_home_eng, R.string.ui_nav_translate_eng, R.string.ui_nav_speech_eng,
            R.string.ui_nav_dictionary_eng, R.string.ui_nav_more_eng,
            R.string.ui_dictionary_message_eng, R.string.ui_placeholder_description_eng,
            R.string.ui_settings_title_eng, R.string.ui_language_eng, R.string.ui_language_summary_eng,
            R.string.ui_language_screen_title_eng, R.string.ui_current_language_eng,
            R.string.ui_app_logo_description_eng,
            R.string.ui_home_badge_eng, R.string.ui_settings_subtitle_eng,
            R.string.ui_supported_languages_eng, R.string.ui_supported_languages_description_eng,
            R.string.ui_recent_dynamic_title_eng, R.string.ui_view_history_eng,
            R.string.ui_dictionary_search_eng, R.string.ui_dictionary_search_description_eng,
            R.string.ui_dictionary_empty_eng, R.string.ui_dictionary_no_results_eng,
            R.string.ui_appearance_eng, R.string.ui_theme_eng,
            R.string.ui_theme_light_eng, R.string.ui_theme_dark_eng,
        )
    }
}
