package com.example.bulosfrontend

object BulosGrammarRuleLoader {
    fun loadRules() {
        val rules = listOf(
            // Batch 2: Questions (D1-D4)
            BulosGrammarRule(
                id = "D1-ANU-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu? -> Ano?",
                condition = { it.trim().equals("Anu?", true) },
                transformation = { "Ano?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D1-ANU-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu? -> What?",
                condition = { it.trim().equals("Anu?", true) },
                transformation = { "What?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D1-ANU-NAME-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu i ngalan mu? -> Ano ang pangalan mo?",
                condition = { it.trim().equals("Anu i ngalan mu?", true) },
                transformation = { "Ano ang pangalan mo?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D1-ANU-NAME-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu i ngalan mu? -> What is your name?",
                condition = { it.trim().equals("Anu i ngalan mu?", true) },
                transformation = { "What is your name?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D1-ANU-FOOD-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu a papangan i pepokaluto mu? -> Anong pagkain ang niluluto mo?",
                condition = { it.trim().equals("Anu a papangan i pepokaluto mu?", true) },
                transformation = { "Anong pagkain ang niluluto mo?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D1-ANU-FOOD-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu a papangan i pepokaluto mu? -> What food are you preparing?",
                condition = { it.trim().equals("Anu a papangan i pepokaluto mu?", true) },
                transformation = { "What food are you preparing?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D2-DENU-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Denu? -> Saan?",
                condition = { it.trim().equals("Denu?", true) },
                transformation = { "Saan?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D2-DENU-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Denu? -> Where?",
                condition = { it.trim().equals("Denu?", true) },
                transformation = { "Where?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D2-DENU-PLACE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Denu un tanuman? -> Nasaan ang halamanan?",
                condition = { it.trim().equals("Denu un tanuman?", true) },
                transformation = { "Nasaan ang halamanan?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D2-DENU-PLACE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Denu un tanuman? -> Where is the garden?",
                condition = { it.trim().equals("Denu un tanuman?", true) },
                transformation = { "Where is the garden?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D3-KAPIDE-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Kapide? -> Kailan?",
                condition = { it.trim().equals("Kapide?", true) },
                transformation = { "Kailan?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D3-KAPIDE-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Kapide? -> When?",
                condition = { it.trim().equals("Kapide?", true) },
                transformation = { "When?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pide? Pide? -> Magkano? Ilan?",
                condition = { it.trim().equals("Pide? Pide?", true) },
                transformation = { "Magkano? Ilan?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Pide? Pide? -> How much? / How many?",
                condition = { it.trim().equals("Pide? Pide?", true) },
                transformation = { "How much? / How many?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-PRICE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pide i oyo? -> Magkano ito?",
                condition = { it.trim().equals("Pide i oyo?", true) },
                transformation = { "Magkano ito?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-PRICE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Pide i oyo? -> How much is it?",
                condition = { it.trim().equals("Pide i oyo?", true) },
                transformation = { "How much is it?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-QUANTITY-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pide un aduwa? -> Magkano ang dalawa?",
                condition = { it.trim().equals("Pide un aduwa?", true) },
                transformation = { "Magkano ang dalawa?" },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-QUANTITY-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Pide un aduwa? -> How much for two?",
                condition = { it.trim().equals("Pide un aduwa?", true) },
                transformation = { "How much for two?" },
                priority = 200,
                isEnabled = true,
            ),

            // Batch 3: Pronoun and possessive patterns (B1-B4)
            BulosGrammarRule(
                id = "B1-POSSESSIVE-DEKU",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dēku i <noun> [a <demo>] -> Sa akin ang <noun> [na <demo>]",
                condition = { text ->
                    Regex("^Dēku i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matches(text.trim())
                },
                transformation = { text ->
                    val match = Regex("^Dēku i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matchEntire(text.trim())
                    if (match != null) {
                        val noun = match.groupValues[1]
                        val demo = match.groupValues[2]
                        val targetNoun = if (noun.equals("silpon", true)) "cellphone" else noun
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa akin ang $targetNoun na $targetDemo" else "Sa akin ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "B2-POSSESSIVE-DIKAW",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikaw i <noun> [a <demo>] -> Sa iyo ang <noun> [na <demo>]",
                condition = { text ->
                    Regex("^Dikaw i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matches(text.trim())
                },
                transformation = { text ->
                    val match = Regex("^Dikaw i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matchEntire(text.trim())
                    if (match != null) {
                        val noun = match.groupValues[1]
                        val demo = match.groupValues[2]
                        val targetNoun = if (noun.equals("silpon", true)) "cellphone" else noun
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa iyo ang $targetNoun na $targetDemo" else "Sa iyo ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "B3-POSSESSIVE-DIKAMI",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikami i <noun> [a <demo>] -> Sa amin ang <noun> [na <demo>]",
                condition = { text ->
                    Regex("^Dikami i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matches(text.trim())
                },
                transformation = { text ->
                    val match = Regex("^Dikami i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matchEntire(text.trim())
                    if (match != null) {
                        val noun = match.groupValues[1]
                        val demo = match.groupValues[2]
                        val targetNoun = if (noun.equals("silpon", true)) "cellphone" else noun
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa amin ang $targetNoun na $targetDemo" else "Sa amin ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "B4-POSSESSIVE-DIYA",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Diya i <noun> [a <demo>] -> Sa kaniya ang <noun> [na <demo>]",
                condition = { text ->
                    Regex("^Diya i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matches(text.trim())
                },
                transformation = { text ->
                    val match = Regex("^Diya i (silpon|demit|bengka|beloy)(?:\\s+a\\s+(oyo|iwina))?$", RegexOption.IGNORE_CASE).matchEntire(text.trim())
                    if (match != null) {
                        val noun = match.groupValues[1]
                        val demo = match.groupValues[2]
                        val targetNoun = if (noun.equals("silpon", true)) "cellphone" else noun
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa kaniya ang $targetNoun na $targetDemo" else "Sa kaniya ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = true,
            ),

            // Batch 4: Sentence Structure (D1-D3)
            BulosGrammarRule(
                id = "D1-DESCRIPTOR-BELOY-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Hanga i beloy ni patod -> Malaki ang bahay ng lalaki",
                condition = { it.trim().equals("Hanga i beloy ni patod", true) },
                transformation = { "Malaki ang bahay ng lalaki" },
                priority = 180,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "D1-DESCRIPTOR-BELOY-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Hanga i beloy ni patod -> The house of man is big",
                condition = { it.trim().equals("Hanga i beloy ni patod", true) },
                transformation = { "The house of man is big" },
                priority = 180,
                isEnabled = true,
            ),

            // Batch 6: Imperatives (E1-E3)
            BulosGrammarRule(
                id = "E1-IMPERATIVE-MO-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Lenisin mo i beloy. -> Linisin mo ang bahay",
                condition = { it.trim().equals("Lenisin mo i beloy.", true) || it.trim().equals("Lenisin mo i beloy", true) },
                transformation = { "Linisin mo ang bahay" },
                priority = 180,
                isEnabled = true,
            ),
            BulosGrammarRule(
                id = "E1-IMPERATIVE-MO-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Lenisin mo i beloy. -> Clean the house",
                condition = { it.trim().equals("Lenisin mo i beloy.", true) || it.trim().equals("Lenisin mo i beloy", true) },
                transformation = { "Clean the house" },
                priority = 180,
                isEnabled = true,
            ),
        )

        for (rule in rules) {
            BulosGrammarRuleRegistry.register(rule)
        }
    }
}
