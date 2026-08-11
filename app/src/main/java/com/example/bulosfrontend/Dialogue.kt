package com.example.bulosfrontend

data class DialogueContent(
    val greetingRes: Int,
    val infoRes: Int,
    val button1Res: Int,
    val button2Res: Int,
    val button3Res: Int,
    val button4Res: Int,
    val footerRes: Int,
)

object DialogueProvider {
    fun getDialogue(key: String): DialogueContent {
        return when (key) {
            "english" -> DialogueContent(
                greetingRes = R.string.greeting_format,
                infoRes = R.string.choose_lang_english,
                button1Res = R.string.main_button_1_eng,
                button2Res = R.string.main_button_2_eng,
                button3Res = R.string.main_button_3_eng,
                button4Res = R.string.main_button_4_eng,
                footerRes = R.string.footer_eng,
            )
            "filipino" -> DialogueContent(
                greetingRes = R.string.greeting_format,
                infoRes = R.string.choose_lang_filipino,
                button1Res = R.string.main_button_1_fil,
                button2Res = R.string.main_button_2_fil,
                button3Res = R.string.main_button_3_fil,
                button4Res = R.string.main_button_4_fil,
                footerRes = R.string.footer_fil,
            )
            "bulos" -> DialogueContent(
                greetingRes = R.string.greeting_format,
                infoRes = R.string.choose_lang_bulos,
                button1Res = R.string.main_button_1_bul,
                button2Res = R.string.main_button_2_bul,
                button3Res = R.string.main_button_3_bul,
                button4Res = R.string.main_button_4_bul,
                footerRes = R.string.footer_bul,
            )
            // on else, default to copy of english text
            else -> DialogueContent(
                greetingRes = R.string.greeting_format,
                infoRes = R.string.choose_lang_english,
                button1Res = R.string.main_button_1_eng,
                button2Res = R.string.main_button_2_eng,
                button3Res = R.string.main_button_3_eng,
                button4Res = R.string.main_button_4_eng,
                footerRes = R.string.footer_eng,
            )
        }
    }
}
