package com.example.bulosfrontend

data class DialogueContent(
    val mainHeaderRes: Int,
    val translateHeaderRes: Int,
    val resultHeaderRes: Int,
    val voiceHeaderRes: Int,
    val infoRes: Int,
    val button1Res: Int,
    val button2Res: Int,
    val button3Res: Int,
    val button4Res: Int,
    val footerRes: Int,
    val sideButton1Res: Int,
    val sideButton2Res: Int,
    val translateActionRes: Int,
    val placeholderRes: Int,
    val backToHomeRes: Int,
    val copiedRes: Int,
    val recordStartLabelRes: Int,
    val cancelBtnRes: Int,
    val goBackRes: Int,
    val languageKey: String,
)

object DialogueProvider {
    private val dialogues = mapOf(
        "english" to DialogueContent(
            R.string.header_main_eng, R.string.header_translate_eng, R.string.header_results_eng,
            R.string.header_voice_eng, R.string.choose_lang_eng, R.string.main_button_1_eng,
            R.string.main_button_2_eng, R.string.main_button_3_eng, R.string.main_button_4_eng,
            R.string.footer_eng, R.string.side_button_1_eng, R.string.side_button_2_eng,
            R.string.translate_action_eng, R.string.placeholder_eng, R.string.back_to_home,
            R.string.copied_msg_eng, R.string.record_start_label_eng, R.string.cancel_btn_eng,
            R.string.go_back_eng, "english",
        ),
        "filipino" to DialogueContent(
            R.string.header_main_fil, R.string.header_translate_fil, R.string.header_results_fil,
            R.string.header_voice_fil, R.string.choose_lang_fil, R.string.main_button_1_fil,
            R.string.main_button_2_fil, R.string.main_button_3_fil, R.string.main_button_4_fil,
            R.string.footer_fil, R.string.side_button_1_fil, R.string.side_button_2_fil,
            R.string.translate_action_fil, R.string.placeholder_fil, R.string.back_to_home,
            R.string.copied_msg_fil, R.string.record_start_label_fil, R.string.cancel_btn_fil,
            R.string.go_back_fil, "filipino",
        ),
        "bulos" to DialogueContent(
            R.string.header_main_bul, R.string.header_translate_bul, R.string.header_results_bul,
            R.string.header_voice_bul, R.string.choose_lang_bul, R.string.main_button_1_bul,
            R.string.main_button_2_bul, R.string.main_button_3_bul, R.string.main_button_4_bul,
            R.string.footer_bul, R.string.side_button_1_bul, R.string.side_button_2_bul,
            R.string.translate_action_bul, R.string.placeholder_bul, R.string.back_to_home,
            R.string.copied_msg_bul, R.string.record_start_label_bul, R.string.cancel_btn_bul,
            R.string.go_back_bul, "bulos",
        ),
    )

    fun getDialogue(key: String) = dialogues[key] ?: dialogues["english"]!!
}
