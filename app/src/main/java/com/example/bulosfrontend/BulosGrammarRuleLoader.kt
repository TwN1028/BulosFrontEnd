package com.example.bulosfrontend

object BulosGrammarRuleLoader {
    fun loadRules() {
        BulosGrammarRuleRegistry.clear()
        val rules = listOf(
            // Batch 2: Questions (D1-D4: 22 rules)
            BulosGrammarRule(
                id = "D1-ANU-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu? -> Ano?",
                condition = { it.trim().equals("Anu?", true) },
                transformation = { "Ano?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-ANU-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu? -> What?",
                condition = { it.trim().equals("Anu?", true) },
                transformation = { "What?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-ANU-NAME-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu i ngalan mu? -> Ano ang pangalan mo?",
                condition = { it.trim().equals("Anu i ngalan mu?", true) },
                transformation = { "Ano ang pangalan mo?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-ANU-NAME-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu i ngalan mu? -> What is your name?",
                condition = { it.trim().equals("Anu i ngalan mu?", true) },
                transformation = { "What is your name?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-ANU-FOOD-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu a papangan i pepokaluto mu? -> Anong pagkain ang niluluto mo?",
                condition = { it.trim().equals("Anu a papangan i pepokaluto mu?", true) },
                transformation = { "Anong pagkain ang niluluto mo?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-ANU-FOOD-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu a papangan i pepokaluto mu? -> What food are you preparing?",
                condition = { it.trim().equals("Anu a papangan i pepokaluto mu?", true) },
                transformation = { "What food are you preparing?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-DENU-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Denu? -> Saan?",
                condition = { it.trim().equals("Denu?", true) },
                transformation = { "Saan?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-DENU-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Denu? -> Where?",
                condition = { it.trim().equals("Denu?", true) },
                transformation = { "Where?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-DENU-PLACE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Denu un tanuman? -> Nasaan ang halamanan?",
                condition = { it.trim().equals("Denu un tanuman?", true) },
                transformation = { "Nasaan ang halamanan?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-DENU-PLACE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Denu un tanuman? -> Where is the garden?",
                condition = { it.trim().equals("Denu un tanuman?", true) },
                transformation = { "Where is the garden?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-DENO-ORIGIN-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Tage deno ka? -> Taga saan ka?",
                condition = { Regex("^Tage d[eē]no ka[?.]?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Taga saan ka?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-DENO-ORIGIN-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Tage deno ka? -> Where are you from?",
                condition = { Regex("^Tage d[eē]no ka[?.]?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Where are you from?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D3-KAPIDE-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Kapide? -> Kailan?",
                condition = { it.trim().equals("Kapide?", true) },
                transformation = { "Kailan?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D3-KAPIDE-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Kapide? -> When?",
                condition = { it.trim().equals("Kapide?", true) },
                transformation = { "When?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D3-KAPIDE-TIME-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Kapide ka dumalong? -> Kailan ka dumating?",
                condition = { Regex("^Kapide ka dumalong[?.]?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Kailan ka dumating?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D3-KAPIDE-TIME-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Kapide ka dumalong? -> When did you come?",
                condition = { Regex("^Kapide ka dumalong[?.]?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "When did you come?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-ROOT-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pide? Pide? -> Magkano? Ilan?",
                condition = { it.trim().equals("Pide? Pide?", true) },
                transformation = { "Magkano? Ilan?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-ROOT-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Pide? Pide? -> How much? / How many?",
                condition = { it.trim().equals("Pide? Pide?", true) },
                transformation = { "How much? / How many?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-PRICE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pide i oyo? -> Magkano ito?",
                condition = { it.trim().equals("Pide i oyo?", true) },
                transformation = { "Magkano ito?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-PRICE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Pide i oyo? -> How much is it?",
                condition = { it.trim().equals("Pide i oyo?", true) },
                transformation = { "How much is it?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-QUANTITY-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pide un aduwa? -> Magkano ang dalawa?",
                condition = { it.trim().equals("Pide un aduwa?", true) },
                transformation = { "Magkano ang dalawa?" },
                priority = 200,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D4-PIDE-QUANTITY-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Pide un aduwa? -> How much for two?",
                condition = { it.trim().equals("Pide un aduwa?", true) },
                transformation = { "How much for two?" },
                priority = 200,
                isEnabled = false,
            ),

            // Batch 3: Pronoun and possessive patterns (B1-B4: 4 rules)
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
                        val targetNoun = when (noun.lowercase()) {
                            "silpon" -> "cellphone"
                            "demit" -> "damit"
                            "bengka" -> "bangka"
                            "beloy" -> "bahay"
                            else -> noun
                        }
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa akin ang $targetNoun na $targetDemo" else "Sa akin ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = false,
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
                        val targetNoun = when (noun.lowercase()) {
                            "silpon" -> "cellphone"
                            "demit" -> "damit"
                            "bengka" -> "bangka"
                            "beloy" -> "bahay"
                            else -> noun
                        }
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa iyo ang $targetNoun na $targetDemo" else "Sa iyo ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = false,
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
                        val targetNoun = when (noun.lowercase()) {
                            "silpon" -> "cellphone"
                            "demit" -> "damit"
                            "bengka" -> "bangka"
                            "beloy" -> "bahay"
                            else -> noun
                        }
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa amin ang $targetNoun na $targetDemo" else "Sa amin ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = false,
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
                        val targetNoun = when (noun.lowercase()) {
                            "silpon" -> "cellphone"
                            "demit" -> "damit"
                            "bengka" -> "bangka"
                            "beloy" -> "bahay"
                            else -> noun
                        }
                        val targetDemo = when (demo.lowercase()) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetDemo.isNotBlank()) "Sa kaniya ang $targetNoun na $targetDemo" else "Sa kaniya ang $targetNoun"
                    } else text
                },
                priority = 200,
                isEnabled = false,
            ),

            // Batch 4: Sentence Structure (D1-D3: 10 rules)
            BulosGrammarRule(
                id = "D1-DESCRIPTOR-BELOY-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Hanga i beloy ni patod -> Malaki ang bahay ng lalaki",
                condition = { it.trim().equals("Hanga i beloy ni patod", true) },
                transformation = { "Malaki ang bahay ng lalaki" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-DESCRIPTOR-BELOY-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Hanga i beloy ni patod -> The house of man is big",
                condition = { it.trim().equals("Hanga i beloy ni patod", true) },
                transformation = { "The house of man is big" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-DESCRIPTOR-SAMOY-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Mokpal i samoy ni Masampat -> Makapal ang buhok ni Masampat",
                condition = { it.trim().equals("Mokpal i samoy ni Masampat", true) },
                transformation = { "Makapal ang buhok ni Masampat" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D1-DESCRIPTOR-SAMOY-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Mokpal i samoy ni Masampat -> Masampat has thick hair",
                condition = { it.trim().equals("Mokpal i samoy ni Masampat", true) },
                transformation = { "Masampat has thick hair" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-LOCATIVE-POSSESSION-DIKITAM-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikitam i beloy de Tumagtag -> Sa atin ang bahay sa Tumagtag",
                condition = { Regex("^Dikitam i beloy de Tumagtag\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Sa atin ang bahay sa Tumagtag" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-LOCATIVE-POSSESSION-DIKITAM-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dikitam i beloy de Tumagtag -> The house in Tumagtag is ours",
                condition = { Regex("^Dikitam i beloy de Tumagtag\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "The house in Tumagtag is ours" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-LOCATIVE-POSSESSION-DIKAMU-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikamu i beloy de Sinilaben -> Sa inyo ang bahay sa Sinilaban",
                condition = { Regex("^Dikamu i beloy de Sinilaben\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Sa inyo ang bahay sa Sinilaban" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D2-LOCATIVE-POSSESSION-DIKAMU-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dikamu i beloy de Sinilaben -> The house in Sinilaban is yours",
                condition = { Regex("^Dikamu i beloy de Sinilaben\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "The house in Sinilaban is yours" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D3-POSSESSIVE-PLURAL-DIDE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Didē i beloy a inon -> Sa kanila ang bahay na iyon",
                condition = { Regex("^Didē i beloy a inon\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Sa kanila ang bahay na iyon" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "D3-POSSESSIVE-PLURAL-DIDE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Didē i beloy a inon -> That house is theirs",
                condition = { Regex("^Didē i beloy a inon\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "That house is theirs" },
                priority = 180,
                isEnabled = false,
            ),

            // Batch 5: Particles and markers (C2-C6: 10 rules)
            BulosGrammarRule(
                id = "C2-NI-RELATIONAL-NAME-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "ngalan ni agla -> pangalan ng tao",
                condition = { Regex("^ngalan ni agla\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "pangalan ng tao" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C2-NI-RELATIONAL-NAME-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "ngalan ni agla -> name of person",
                condition = { Regex("^ngalan ni agla\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "name of person" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C3-NI-VS-NUN-NEAR-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pegemit i ni bege i petokuy a ge surot ay de alani diya. -> Ginagamit ang ni kung ang tinutukoy ng nagsasalita ay malapit sa kanya",
                condition = { Regex("^Pegemit i ni bege i petokuy a ge surot ay de alani diya\\.?$", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ginagamit ang ni kung ang tinutukoy ng nagsasalita ay malapit sa kanya" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C3-NI-VS-NUN-FAR-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Pegemit i nun bege i petokuy a ge surot ay de alayu diya. -> Ginagamit ang nun kung ang tinutukoy ng nagsasalita ay malayo sa kanya",
                condition = { Regex("^Pegemit i nun bege i petokuy a ge surot ay de alayu diya\\.?$", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ginagamit ang nun kung ang tinutukoy ng nagsasalita ay malayo sa kanya" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C4-DE-LOCATION-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Noduman i eya de tindehan -> Pupunta siya sa tindahan",
                condition = { Regex("^Noduman i eya de tindehan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Pupunta siya sa tindahan" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C4-DE-LOCATION-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Noduman i eya de tindehan -> He is going to the store",
                condition = { Regex("^Noduman i eya de tindehan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "He is going to the store" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C5-I-AS-TITLE-ARTICLE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos -> Ang Pamamaraan ng Pagsulat sa Wikang Dumaget Bulos",
                condition = { Regex("^I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ang Pamamaraan ng Pagsulat sa Wikang Dumaget Bulos" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C5-I-AS-TITLE-ARTICLE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos -> Introduction to Dumaget Bulos Orthography",
                condition = { Regex("^I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Introduction to Dumaget Bulos Orthography" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C6-A-LINKER-DEMONSTRATIVE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Binoy děku i alit a oyo -> Ibinigay sa akin ang damit na ito",
                condition = { Regex("^Binoy d[eēě]ku i alit a oyo\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ibinigay sa akin ang damit na ito" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C6-A-LINKER-DEMONSTRATIVE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Binoy děku i alit a oyo -> This dress is given to me",
                condition = { Regex("^Binoy d[eēě]ku i alit a oyo\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "This dress is given to me" },
                priority = 180,
                isEnabled = false,
            ),

            // Batch 6: Imperatives (E1-E3: 6 rules)
            BulosGrammarRule(
                id = "E1-IMPERATIVE-MO-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Lenisin mo i beloy. -> Linisin mo ang bahay",
                condition = { Regex("^Lenisin mo i beloy\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Linisin mo ang bahay" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "E1-IMPERATIVE-MO-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Lenisin mo i beloy. -> Clean the house",
                condition = { Regex("^Lenisin mo i beloy\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Clean the house" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "E2-IMPERATIVE-YU-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Tapusin yu i yedein. -> Tapusin ninyo ang gawain",
                condition = { Regex("^Tapusin yu i yedein\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Tapusin ninyo ang gawain" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "E2-IMPERATIVE-YU-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Tapusin yu i yedein. -> Finish your work",
                condition = { Regex("^Tapusin yu i yedein\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Finish your work" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "E3-IMPERATIVE-TAM-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Amitin tam un ikan. -> Kuhanin natin ang isda",
                condition = { Regex("^Amitin tam un ikan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Kuhanin natin ang isda" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "E3-IMPERATIVE-TAM-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Amitin tam un ikan. -> Let us get the fish",
                condition = { Regex("^Amitin tam un ikan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Let us get the fish" },
                priority = 180,
                isEnabled = false,
            ),

            // Batch 7: Giving and transfer (F1-F5: 10 rules)
            BulosGrammarRule(
                id = "F1-GIVE-MO-ITEM-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Biyen mo i eya ni ikan. -> Bigyan mo siya ng isda",
                condition = { Regex("^Biyen mo i eya ni ikan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Bigyan mo siya ng isda" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F1-GIVE-MO-ITEM-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Biyen mo i eya ni ikan. -> Give him/her fish",
                condition = { Regex("^Biyen mo i eya ni ikan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Give him/her fish" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F2-GIVE-TAM-TO-CHILDREN-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Biyen tam ni sinaing un manganak. -> Bigyan natin ng kanin ang mga bata",
                condition = { Regex("^Biyen tam ni sinaing un manganak\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Bigyan natin ng kanin ang mga bata" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F2-GIVE-TAM-TO-CHILDREN-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Biyen tam ni sinaing un manganak. -> Let us give rice to the children",
                condition = { Regex("^Biyen tam ni sinaing un manganak\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Let us give rice to the children" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F3-GIVE-YU-FOOD-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Boyin yu un papangan nunde manganak. -> Ibigay ninyo ang pagkain sa mga bata",
                condition = { Regex("^Boyin yu un papangan nunde manganak\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ibigay ninyo ang pagkain sa mga bata" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F3-GIVE-YU-FOOD-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Boyin yu un papangan nunde manganak. -> Give the foods to the children",
                condition = { Regex("^Boyin yu un papangan nunde manganak\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Give the foods to the children" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F4-GIVE-RECIPIENT-PLURAL-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Binoy mi un kapapelan dide. -> Ibinigay namin ang mga papel sa kanila",
                condition = { Regex("^Binoy mi un kapapelan dide\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ibinigay namin ang mga papel sa kanila" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F4-GIVE-RECIPIENT-PLURAL-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Binoy mi un kapapelan dide. -> We gave the papers to them",
                condition = { Regex("^Binoy mi un kapapelan dide\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "We gave the papers to them" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F5-GIVE-THIRD-PERSON-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Binoy na un bokan. -> Ibinigay niya ang bulaklak",
                condition = { Regex("^Binoy na un bokan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Ibinigay niya ang bulaklak" },
                priority = 180,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "F5-GIVE-THIRD-PERSON-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Binoy na un bokan. -> He/She gave the flower",
                condition = { Regex("^Binoy na un bokan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "He/She gave the flower" },
                priority = 180,
                isEnabled = false,
            ),

            // Batch 8: Verb and quantity examples (G1-G3: 6 rules)
            BulosGrammarRule(
                id = "G1-NAGYEDI-PAST-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Nagyedi ok ni beloy/Yumadi ok ni beloy -> Gumawa ako ng bahay",
                condition = { Regex("^Nagyedi ok ni beloy/Yumadi ok ni beloy\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Gumawa ako ng bahay" },
                priority = 190,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "G1-NAGYEDI-PAST-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Nagyedi ok ni beloy/Yumadi ok ni beloy -> I built house",
                condition = { Regex("^Nagyedi ok ni beloy/Yumadi ok ni beloy\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "I built house" },
                priority = 190,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "G2-GEHANDE-PROGRESSIVE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Gehande ok ni papangan -> Naghahanda ako ng pagkain",
                condition = { Regex("^Gehande ok ni papangan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Naghahanda ako ng pagkain" },
                priority = 190,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "G2-GEHANDE-PROGRESSIVE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Gehande ok ni papangan -> I am preparing foods",
                condition = { Regex("^Gehande ok ni papangan\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "I am preparing foods" },
                priority = 190,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "G3-NANGAMITOK-QUANTITY-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Nangamitok ni aduwa -> Kukuha ako ng dalawa",
                condition = { Regex("^Nangamitok ni aduwa\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "Kukuha ako ng dalawa" },
                priority = 190,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "G3-NANGAMITOK-QUANTITY-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Nangamitok ni aduwa -> I will get two",
                condition = { Regex("^Nangamitok ni aduwa\\.?", RegexOption.IGNORE_CASE).matches(it.trim()) },
                transformation = { "I will get two" },
                priority = 190,
                isEnabled = false,
            ),

            // Batch 9: Orthography safeguards (C1-C6 identity preservation guards: 6 rules)
            BulosGrammarRule(
                id = "C1-HYPHEN-PRESERVE",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Hyphen preservation guard",
                condition = { Regex("\\b\\p{L}+-\\p{L}+\\b").containsMatchIn(it) },
                transformation = { it },
                priority = 250,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C2-APOSTROPHE-PRESERVE",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Apostrophe preservation guard",
                condition = { Regex("\\b\\p{L}+['’‘＇ʼ]\\p{L}+\\b").containsMatchIn(it) },
                transformation = { it },
                priority = 250,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C3-DIACRITIC-PRESERVE",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Diacritic preservation guard",
                condition = { Regex("[ēěëāīōūéèêáàâóòôúùûíìî]").containsMatchIn(it) },
                transformation = { it },
                priority = 250,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C4-PROPER-NAME-PRESERVE",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Proper name preservation guard",
                condition = { Regex("\\b[A-Z][a-zA-Z]*\\b").containsMatchIn(it) },
                transformation = { it },
                priority = 250,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C5-LOANWORD-PRESERVE",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Loanword preservation guard",
                condition = { Regex("\\b[a-z]+[A-Z][a-zA-Z]*\\b").containsMatchIn(it) },
                transformation = { it },
                priority = 250,
                isEnabled = false,
            ),
            BulosGrammarRule(
                id = "C6-REPEATED-ROOT-PRESERVE",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Repeated root preservation guard",
                condition = { Regex("\\b(\\p{L}+)[-\\s]+\\1\\b", RegexOption.IGNORE_CASE).containsMatchIn(it) },
                transformation = { it },
                priority = 250,
                isEnabled = false,
            ),
        )

        for (rule in rules) {
            BulosGrammarRuleRegistry.register(rule)
        }
    }

    /** Explicit live-test configuration loader. Enables all 74 central production rules and registers/enables the 20 validated examples. */
    fun loadLiveTestRules() {
        loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)
        loadValidatedExamples(isEnabled = true)
        BulosGrammarRuleRegistry.isGrammarRulesEnabled = true
    }

    /** Separate opt-in loader for the 20 validated examples / candidates. Not part of default production registry. */
    fun loadValidatedExamples(isEnabled: Boolean = false) {
        val examples = listOf(
            BulosGrammarRule(
                id = "E1-QUESTION-NAME-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Anu i ngalan mo? -> Ano ang pangalan mo?",
                condition = { it.trim() == "Anu i ngalan mo?" },
                transformation = { "Ano ang pangalan mo?" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "E1-QUESTION-NAME-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Anu i ngalan mo? -> What is your name?",
                condition = { it.trim() == "Anu i ngalan mo?" },
                transformation = { "What is your name?" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "D2-DEMONSTRATIVE-YOURS-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikaw i silpon a oyo -> Sa iyo ang cellphone na ito",
                condition = { it.trim() == "Dikaw i silpon a oyo" },
                transformation = { "Sa iyo ang cellphone na ito" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "D2-DEMONSTRATIVE-YOURS-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dikaw i silpon a oyo -> This cellphone is yours",
                condition = { it.trim() == "Dikaw i silpon a oyo" },
                transformation = { "This cellphone is yours" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "D2-DEMONSTRATIVE-MINE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dēku i silpon a oyo -> Sa akin ang cellphone na ito",
                condition = { it.trim() == "Dēku i silpon a oyo" },
                transformation = { "Sa akin ang cellphone na ito" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "D2-DEMONSTRATIVE-MINE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dēku i silpon a oyo -> This cellphone is mine",
                condition = { it.trim() == "Dēku i silpon a oyo" },
                transformation = { "This cellphone is mine" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "M2-HAIR-THICK-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Mokpal i samoy ni Masampat -> Makapal ang buhok ni Masampat",
                condition = { it.trim() == "Mokpal i samoy ni Masampat" },
                transformation = { "Makapal ang buhok ni Masampat" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "M2-HAIR-THICK-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Mokpal i samoy ni Masampat -> Masampat has thick hair",
                condition = { it.trim() == "Mokpal i samoy ni Masampat" },
                transformation = { "Masampat has thick hair" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P3-POSSESSIVE-MINE-GARDEN-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dëku un tanuman. -> Sa akin ang halamanan.",
                condition = { it.trim() == "Dëku un tanuman." },
                transformation = { "Sa akin ang halamanan." },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P3-POSSESSIVE-MINE-GARDEN-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dëku un tanuman. -> It is my garden.",
                condition = { it.trim() == "Dëku un tanuman." },
                transformation = { "It is my garden." },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P4-POSSESSIVE-MINE-HOUSE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Beloy ko nay. -> Bahay ko iyan.",
                condition = { it.trim() == "Beloy ko nay." },
                transformation = { "Bahay ko iyan." },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P4-POSSESSIVE-MINE-HOUSE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Beloy ko nay. -> That is my house.",
                condition = { it.trim() == "Beloy ko nay." },
                transformation = { "That is my house." },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "Q3-WHOSE-HOUSE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dinu a beloy i wina? -> Kaninong bahay iyan?",
                condition = { it.trim() == "Dinu a beloy i wina?" },
                transformation = { "Kaninong bahay iyan?" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "Q3-WHOSE-HOUSE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dinu a beloy i wina? -> Whose house is that?",
                condition = { it.trim() == "Dinu a beloy i wina?" },
                transformation = { "Whose house is that?" },
                priority = 210,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P1-POSSESSIVE-OURS-BANGKA-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikami i bengka -> Sa amin ang bangka",
                condition = { it.trim() == "Dikami i bengka" },
                transformation = { "Sa amin ang bangka" },
                priority = 205,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P1-POSSESSIVE-OURS-BANGKA-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dikami i bengka -> The boat that is ours",
                condition = { it.trim() == "Dikami i bengka" },
                transformation = { "The boat that is ours" },
                priority = 205,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P2-POSSESSIVE-OURS-HOUSE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Dikami i beloy de Tagpis -> Sa amin ang bahay sa Tagpis",
                condition = { it.trim() == "Dikami i beloy de Tagpis" },
                transformation = { "Sa amin ang bahay sa Tagpis" },
                priority = 205,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "P2-POSSESSIVE-OURS-HOUSE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Dikami i beloy de Tagpis -> The house in Tagpis is ours",
                condition = { it.trim() == "Dikami i beloy de Tagpis" },
                transformation = { "The house in Tagpis is ours" },
                priority = 205,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "BULOS-CANDIDATE-POSSESSIVE-EN",
                direction = RuleTranslationDirection.BULOS_TO_ENGLISH,
                description = "Candidate possessive pattern EN: <pronoun> i <noun> a <demo>",
                condition = { text ->
                    Regex("^(dēku|dikaw|dikami|diya)\\s+i\\s+(silpon|demit|bengka|beloy)\\s+a\\s+(oyo|iwina)$", RegexOption.IGNORE_CASE).matches(text.trim())
                },
                transformation = { text ->
                    val match = Regex("^(dēku|dikaw|dikami|diya)\\s+i\\s+(silpon|demit|bengka|beloy)\\s+a\\s+(oyo|iwina)$", RegexOption.IGNORE_CASE).matchEntire(text.trim())
                    if (match != null) {
                        val pronoun = match.groupValues[1].lowercase()
                        val noun = match.groupValues[2].lowercase()
                        val demo = match.groupValues[3].lowercase()

                        val targetPossessive = when (pronoun) {
                            "dēku" -> "mine"
                            "dikaw" -> "yours"
                            "dikami" -> "ours"
                            "diya" -> "hers/his"
                            else -> ""
                        }
                        val targetNoun = when (noun) {
                            "silpon" -> "cellphone"
                            "demit" -> "dress"
                            "bengka" -> "boat"
                            "beloy" -> "house"
                            else -> ""
                        }
                        val targetDemo = when (demo) {
                            "oyo" -> "This"
                            "iwina" -> "That"
                            else -> ""
                        }
                        if (targetPossessive.isNotBlank() && targetNoun.isNotBlank() && targetDemo.isNotBlank()) {
                            "$targetDemo $targetNoun is $targetPossessive"
                        } else text
                    } else text
                },
                priority = 180,
                isEnabled = isEnabled,
            ),
            BulosGrammarRule(
                id = "BULOS-CANDIDATE-POSSESSIVE-TL",
                direction = RuleTranslationDirection.BULOS_TO_FILIPINO,
                description = "Candidate possessive pattern TL: <pronoun> i <noun> a <demo>",
                condition = { text ->
                    Regex("^(dēku|dikaw|dikami|diya)\\s+i\\s+(silpon|demit|bengka|beloy)\\s+a\\s+(oyo|iwina)$", RegexOption.IGNORE_CASE).matches(text.trim())
                },
                transformation = { text ->
                    val match = Regex("^(dēku|dikaw|dikami|diya)\\s+i\\s+(silpon|demit|bengka|beloy)\\s+a\\s+(oyo|iwina)$", RegexOption.IGNORE_CASE).matchEntire(text.trim())
                    if (match != null) {
                        val pronoun = match.groupValues[1].lowercase()
                        val noun = match.groupValues[2].lowercase()
                        val demo = match.groupValues[3].lowercase()

                        val targetPossessive = when (pronoun) {
                            "dēku" -> "Sa akin"
                            "dikaw" -> "Sa iyo"
                            "dikami" -> "Sa amin"
                            "diya" -> "Sa kaniya"
                            else -> ""
                        }
                        val targetNoun = when (noun) {
                            "silpon" -> "cellphone"
                            "demit" -> "damit"
                            "bengka" -> "bangka"
                            "beloy" -> "bahay"
                            else -> ""
                        }
                        val targetDemo = when (demo) {
                            "oyo" -> "ito"
                            "iwina" -> "iyan"
                            else -> ""
                        }
                        if (targetPossessive.isNotBlank() && targetNoun.isNotBlank() && targetDemo.isNotBlank()) {
                            "$targetPossessive ang $targetNoun na $targetDemo"
                        } else text
                    } else text
                },
                priority = 180,
                isEnabled = isEnabled,
            ),
        )
        for (rule in examples) {
            BulosGrammarRuleRegistry.register(rule)
        }
    }
}
