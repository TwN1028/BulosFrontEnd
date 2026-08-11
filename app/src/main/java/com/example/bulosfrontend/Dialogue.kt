package com.example.bulosfrontend

data class DialogueContent(
    val greetingRes: Int, val infoRes: Int,
    val button1Res: Int, val button2Res: Int,
    val button3Res: Int, val button4Res: Int,
    val footerRes: Int,
    val sideButton1Res: Int,
    val sideButton2Res: Int,
    val translateActionRes: Int,
    val placeholderRes: Int,
    val languageKey: String,
)

object DialogueProvider {
    private val dialogues = mapOf(
        "english" to DialogueContent(
            R.string.greeting_format, R.string.choose_lang_english,
            R.string.main_button_1_eng, R.string.main_button_2_eng,
            R.string.main_button_3_eng, R.string.main_button_4_eng,
            R.string.footer_eng, R.string.side_button_1_eng,
            R.string.side_button_2_eng, R.string.translate_action_eng,
            R.string.placeholder_eng, "english"
        ),
        "filipino" to DialogueContent(
            R.string.greeting_format, R.string.choose_lang_filipino,
            R.string.main_button_1_fil, R.string.main_button_2_fil,
            R.string.main_button_3_fil, R.string.main_button_4_fil,
            R.string.footer_fil, R.string.side_button_1_fil,
            R.string.side_button_2_fil, R.string.translate_action_fil,
            R.string.placeholder_fil, "filipino"
        ),
        "bulos" to DialogueContent(
            R.string.greeting_format, R.string.choose_lang_bulos,
            R.string.main_button_1_bul, R.string.main_button_2_bul,
            R.string.main_button_3_bul, R.string.main_button_4_bul,
            R.string.footer_bul, R.string.side_button_1_bul,
            R.string.side_button_2_bul, R.string.translate_action_bul,
            R.string.placeholder_bul, "bulos"
        )
    )

    fun getDialogue(key: String) = dialogues[key] ?: dialogues["english"]!!
}
