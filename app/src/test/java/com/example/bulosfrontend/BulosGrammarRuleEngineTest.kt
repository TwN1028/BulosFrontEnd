package com.example.bulosfrontend

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BulosGrammarRuleEngineTest {

    private val engine = BulosGrammarRuleEngine()

    @Before
    fun setUp() {
        BulosGrammarRuleRegistry.clear()
    }

    @After
    fun tearDown() {
        BulosGrammarRuleRegistry.clear()
    }

    @Test
    fun `production registry has exactly 74 rules all disabled by default`() {
        BulosGrammarRuleLoader.loadRules()
        val allRules = BulosGrammarRuleRegistry.getAllRules()

        assertEquals(74, allRules.size)
        assertTrue(BulosGrammarRuleRegistry.isGrammarRulesEnabled)
        assertTrue(allRules.all { !it.isEnabled })

        // Effective active rules in default production mode is 0
        assertEquals(0, BulosGrammarRuleRegistry.getRules(RuleTranslationDirection.BULOS_TO_FILIPINO).size)
        assertEquals(0, BulosGrammarRuleRegistry.getRules(RuleTranslationDirection.BULOS_TO_ENGLISH).size)
    }

    @Test
    fun `live test configuration enables all 94 rules without duplicate IDs`() {
        BulosGrammarRuleLoader.loadLiveTestRules()
        val allRules = BulosGrammarRuleRegistry.getAllRules()

        assertEquals(94, allRules.size)
        assertTrue(BulosGrammarRuleRegistry.isGrammarRulesEnabled)
        assertTrue(allRules.all { it.isEnabled })

        val uniqueIds = allRules.map { it.id }.toSet()
        assertEquals(94, uniqueIds.size)
    }

    @Test
    fun `four additional question rules are registered in production registry`() {
        BulosGrammarRuleLoader.loadRules()
        val registry = BulosGrammarRuleRegistry.getAllRules()

        val d2Tl = registry.find { it.id == "D2-DENO-ORIGIN-TL" }
        val d2En = registry.find { it.id == "D2-DENO-ORIGIN-EN" }
        val d3Tl = registry.find { it.id == "D3-KAPIDE-TIME-TL" }
        val d3En = registry.find { it.id == "D3-KAPIDE-TIME-EN" }

        assertNotNull(d2Tl)
        assertNotNull(d2En)
        assertNotNull(d3Tl)
        assertNotNull(d3En)

        assertEquals(RuleTranslationDirection.BULOS_TO_FILIPINO, d2Tl?.direction)
        assertEquals(RuleTranslationDirection.BULOS_TO_ENGLISH, d2En?.direction)
        assertEquals(RuleTranslationDirection.BULOS_TO_FILIPINO, d3Tl?.direction)
        assertEquals(RuleTranslationDirection.BULOS_TO_ENGLISH, d3En?.direction)

        assertFalse(d2Tl!!.isEnabled)
        assertFalse(d2En!!.isEnabled)
        assertFalse(d3Tl!!.isEnabled)
        assertFalse(d3En!!.isEnabled)
    }

    @Test
    fun `validated 20 examples are separate from production registry`() {
        BulosGrammarRuleLoader.loadRules()
        val prodRuleIds = BulosGrammarRuleRegistry.getAllRules().map { it.id }.toSet()

        assertFalse(prodRuleIds.contains("BULOS-CANDIDATE-POSSESSIVE-EN"))
        assertFalse(prodRuleIds.contains("BULOS-CANDIDATE-POSSESSIVE-TL"))
        assertFalse(prodRuleIds.contains("E1-QUESTION-NAME-TL"))

        // Opting into validated examples registers them explicitly
        BulosGrammarRuleLoader.loadValidatedExamples(isEnabled = true)
        val combinedRules = BulosGrammarRuleRegistry.getAllRules()
        assertEquals(94, combinedRules.size)
        assertNotNull(combinedRules.find { it.id == "BULOS-CANDIDATE-POSSESSIVE-EN" })
    }

    @Test
    fun `global feature flag or per rule enablement independently disable rules`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        // With rules enabled, active rules are non-empty
        assertTrue(BulosGrammarRuleRegistry.getRules(RuleTranslationDirection.BULOS_TO_FILIPINO).isNotEmpty())

        // Disabling global flag turns off all rule processing
        BulosGrammarRuleRegistry.isGrammarRulesEnabled = false
        assertEquals(0, BulosGrammarRuleRegistry.getRules(RuleTranslationDirection.BULOS_TO_FILIPINO).size)
    }

    @Test
    fun `rules are disabled by default`() {
        val rule = BulosGrammarRule(
            id = "test-rule-1",
            direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
            description = "Test rule",
            condition = { it.contains("test") },
            transformation = { it.replace("test", "passed") },
            priority = 1,
            isEnabled = false,
        )
        BulosGrammarRuleRegistry.register(rule)

        val result = engine.applyRules("this is a test input", RuleTranslationDirection.ENGLISH_TO_BULOS)
        assertEquals("this is a test input", result)
    }

    @Test
    fun `explicitly enabled rules execute in priority order`() {
        val r1 = BulosGrammarRule(
            id = "r1",
            direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
            description = "r1",
            condition = { it.contains("hello") },
            transformation = { it + " world" },
            priority = 1,
            isEnabled = true,
        )
        val r2 = BulosGrammarRule(
            id = "r2",
            direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
            description = "r2",
            condition = { it.contains("world") },
            transformation = { it.uppercase() },
            priority = 2,
            isEnabled = true,
        )
        BulosGrammarRuleRegistry.clear()
        BulosGrammarRuleRegistry.register(r1)
        BulosGrammarRuleRegistry.register(r2)

        val result = engine.applyRules("hello", RuleTranslationDirection.ENGLISH_TO_BULOS)
        assertEquals("HELLO WORLD", result)
    }

    @Test
    fun `translation direction filtering is respected`() {
        val enRule = BulosGrammarRule(
            id = "en-rule",
            direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
            description = "EN rule",
            condition = { it.contains("dog") },
            transformation = { it.replace("dog", "cat") },
            priority = 1,
            isEnabled = true,
        )
        BulosGrammarRuleRegistry.register(enRule)

        val result = engine.applyRules("big dog", RuleTranslationDirection.FILIPINO_TO_BULOS)
        assertEquals("big dog", result)
    }

    @Test
    fun `non matching inputs and output preservation work correctly`() {
        val rule = BulosGrammarRule(
            id = "match-rule",
            direction = RuleTranslationDirection.ENGLISH_TO_BULOS,
            description = "Match rule",
            condition = { it.contains("target") },
            transformation = { it.replace("target", "success") },
            priority = 1,
            isEnabled = true,
        )
        BulosGrammarRuleRegistry.register(rule)

        val result = engine.applyRules("unrelated input text 123", RuleTranslationDirection.ENGLISH_TO_BULOS)
        assertEquals("unrelated input text 123", result)
    }

    @Test
    fun `batch 3 possessive rules B1 to B4 transform correctly when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        val r1 = engine.applyRules("Dēku i beloy a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa akin ang bahay na ito", r1)

        val r2 = engine.applyRules("Dikaw i silpon", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa iyo ang cellphone", r2)

        val r3 = engine.applyRules("Dikami i demit a iwina", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa amin ang damit na iyan", r3)

        val r4 = engine.applyRules("Diya i bengka", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa kaniya ang bangka", r4)
    }

    @Test
    fun `batch 3 possessive rules reject near misses and unsupported nouns`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        val nearMiss1 = engine.applyRules("Dēku i kotse a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Dēku i kotse a oyo", nearMiss1)

        val nearMiss2 = engine.applyRules("Dikaw i beloy a invalid", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Dikaw i beloy a invalid", nearMiss2)
    }

    @Test
    fun `batch 4 sentence structure rules transform correctly when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        val r1 = engine.applyRules("Mokpal i samoy ni Masampat", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Makapal ang buhok ni Masampat", r1)

        val r2 = engine.applyRules("Dikitam i beloy de Tumagtag", RuleTranslationDirection.BULOS_TO_ENGLISH)
        assertEquals("The house in Tumagtag is ours", r2)

        val r3 = engine.applyRules("Dikamu i beloy de Sinilaben", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa inyo ang bahay sa Sinilaban", r3)

        val r4 = engine.applyRules("Didē i beloy a inon", RuleTranslationDirection.BULOS_TO_ENGLISH)
        assertEquals("That house is theirs", r4)
    }

    @Test
    fun `particle and marker C2 to C6 exact constructions transform correctly in both directions when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        // C2: Relational marker ni
        assertEquals("pangalan ng tao", engine.applyRules("ngalan ni agla", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("name of person", engine.applyRules("ngalan ni agla", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // C3: ni vs nun context distinction
        assertEquals("Ginagamit ang ni kung ang tinutukoy ng nagsasalita ay malapit sa kanya",
            engine.applyRules("Pegemit i ni bege i petokuy a ge surot ay de alani diya.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Ginagamit ang nun kung ang tinutukoy ng nagsasalita ay malayo sa kanya",
            engine.applyRules("Pegemit i nun bege i petokuy a ge surot ay de alayu diya.", RuleTranslationDirection.BULOS_TO_FILIPINO))

        // C4: Locative marker de
        assertEquals("Pupunta siya sa tindahan", engine.applyRules("Noduman i eya de tindehan", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("He is going to the store", engine.applyRules("Noduman i eya de tindehan", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // C5: Title or article marker i
        assertEquals("Ang Pamamaraan ng Pagsulat sa Wikang Dumaget Bulos",
            engine.applyRules("I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Introduction to Dumaget Bulos Orthography",
            engine.applyRules("I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // C6: Demonstrative linker a
        assertEquals("Ibinigay sa akin ang damit na ito", engine.applyRules("Binoy děku i alit a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("This dress is given to me", engine.applyRules("Binoy dēku i alit a oyo", RuleTranslationDirection.BULOS_TO_ENGLISH))
        assertEquals("This dress is given to me", engine.applyRules("Binoy deku i alit a oyo", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `imperative E1 to E3 rules transform exact constructions in both directions when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        // E1 regression
        assertEquals("Linisin mo ang bahay", engine.applyRules("Lenisin mo i beloy.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Clean the house", engine.applyRules("Lenisin mo i beloy", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // E2: Tapusin yu i yedein
        assertEquals("Tapusin ninyo ang gawain", engine.applyRules("Tapusin yu i yedein.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Finish your work", engine.applyRules("Tapusin yu i yedein.", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // E3: Amitin tam un ikan
        assertEquals("Kuhanin natin ang isda", engine.applyRules("Amitin tam un ikan.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Let us get the fish", engine.applyRules("Amitin tam un ikan.", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `giving and transfer F1 to F5 rules transform exact constructions in both directions when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        // F1
        assertEquals("Bigyan mo siya ng isda", engine.applyRules("Biyen mo i eya ni ikan.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Give him/her fish", engine.applyRules("Biyen mo i eya ni ikan.", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // F5
        assertEquals("Ibinigay niya ang bulaklak", engine.applyRules("Binoy na un bokan.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("He/She gave the flower", engine.applyRules("Binoy na un bokan.", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `verb aspect and quantity G1 to G3 rules transform exact constructions in both directions when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        // G1
        assertEquals("Gumawa ako ng bahay", engine.applyRules("Nagyedi ok ni beloy/Yumadi ok ni beloy.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("I built house", engine.applyRules("Nagyedi ok ni beloy/Yumadi ok ni beloy.", RuleTranslationDirection.BULOS_TO_ENGLISH))

        // G2
        assertEquals("Naghahanda ako ng pagkain", engine.applyRules("Gehande ok ni papangan.", RuleTranslationDirection.BULOS_TO_FILIPINO))

        // G3
        assertEquals("Kukuha ako ng dalawa", engine.applyRules("Nangamitok ni aduwa.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("I will get two", engine.applyRules("Nangamitok ni aduwa.", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `additional question rules D2 origin and D3 time transform when enabled`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)

        assertEquals("Taga saan ka?", engine.applyRules("Tage deno ka?", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Taga saan ka?", engine.applyRules("Tage dēno ka?", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Where are you from?", engine.applyRules("Tage deno ka?", RuleTranslationDirection.BULOS_TO_ENGLISH))

        assertEquals("Kailan ka dumating?", engine.applyRules("Kapide ka dumalong?", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("When did you come?", engine.applyRules("Kapide ka dumalong?", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `orthography safeguards C1 to C6 are registered and do not interfere with exact translation`() {
        BulosGrammarRuleLoader.loadRules()

        val c1 = BulosGrammarRuleRegistry.getAllRules().find { it.id == "C1-HYPHEN-PRESERVE" }
        val c2 = BulosGrammarRuleRegistry.getAllRules().find { it.id == "C2-APOSTROPHE-PRESERVE" }
        val c3 = BulosGrammarRuleRegistry.getAllRules().find { it.id == "C3-DIACRITIC-PRESERVE" }
        val c4 = BulosGrammarRuleRegistry.getAllRules().find { it.id == "C4-PROPER-NAME-PRESERVE" }
        val c5 = BulosGrammarRuleRegistry.getAllRules().find { it.id == "C5-LOANWORD-PRESERVE" }
        val c6 = BulosGrammarRuleRegistry.getAllRules().find { it.id == "C6-REPEATED-ROOT-PRESERVE" }

        assertNotNull(c1)
        assertNotNull(c2)
        assertNotNull(c3)
        assertNotNull(c4)
        assertNotNull(c5)
        assertNotNull(c6)

        assertFalse(c1!!.isEnabled)
        assertFalse(c2!!.isEnabled)
        assertFalse(c3!!.isEnabled)
        assertFalse(c4!!.isEnabled)
        assertFalse(c5!!.isEnabled)
        assertFalse(c6!!.isEnabled)

        assertTrue(c1.condition("coat-bul"))
        assertTrue(c2.condition("Ma'abig"))
        assertTrue(c3.condition("dēku"))
        assertTrue(c4.condition("Masampat"))
        assertTrue(c5.condition("smartPhone"))
        assertTrue(c6.condition("pide-pide"))
    }

    @Test
    fun `offline pipeline preserves protected tokens while translating surrounding supported words`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleRegistry.setAllRulesEnabled(true)
        val dict = OfflineDictionary(
            listOf(
                mapOf("English" to "good morning", "Filipino" to "magandang umaga", "Bulos" to "maabig a buklas"),
                mapOf("English" to "friend", "Filipino" to "kaibigan", "Bulos" to "gayyem"),
                mapOf("English" to "Mother-in-law", "Filipino" to "biyenan", "Bulos" to "Ma'abig"),
            ),
        )

        val nameResult = dict.lookup("good morning Angelo!", UiLanguage.ENGLISH, UiLanguage.BULOS)
        assertEquals("Maabig a buklas Angelo!", (nameResult as OfflineLookupResult.Translation).translatedText)

        val apostropheResult = dict.lookup("Ma'abig", UiLanguage.BULOS, UiLanguage.ENGLISH)
        assertEquals("Mother-in-law", (apostropheResult as OfflineLookupResult.Translation).translatedText)

        val diacriticResult = engine.applyRules("Dēku i silpon a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa akin ang cellphone na ito", diacriticResult)

        val properNameRuleResult = engine.applyRules("Mokpal i samoy ni Masampat", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Makapal ang buhok ni Masampat", properNameRuleResult)
    }

    @Test
    fun `validated 18 exact phrase mappings transform correctly in both directions when opted in`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleLoader.loadValidatedExamples(isEnabled = true)

        assertEquals("Ano ang pangalan mo?", engine.applyRules("Anu i ngalan mo?", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("What is your name?", engine.applyRules("Anu i ngalan mo?", RuleTranslationDirection.BULOS_TO_ENGLISH))
        assertEquals("Sa iyo ang cellphone na ito", engine.applyRules("Dikaw i silpon a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("This cellphone is yours", engine.applyRules("Dikaw i silpon a oyo", RuleTranslationDirection.BULOS_TO_ENGLISH))
        assertEquals("Sa akin ang halamanan.", engine.applyRules("Dëku un tanuman.", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("It is my garden.", engine.applyRules("Dëku un tanuman.", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `validated constrained possessive candidate patterns handle supported and reject unsupported forms when opted in`() {
        BulosGrammarRuleLoader.loadRules()
        BulosGrammarRuleLoader.loadValidatedExamples(isEnabled = true)

        assertEquals("This cellphone is mine", engine.applyRules("Dēku i silpon a oyo", RuleTranslationDirection.BULOS_TO_ENGLISH))
        assertEquals("That dress is yours", engine.applyRules("Dikaw i demit a iwina", RuleTranslationDirection.BULOS_TO_ENGLISH))
        assertEquals("Sa akin ang cellphone na ito", engine.applyRules("Dēku i silpon a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO))
        assertEquals("Sa iyo ang damit na iyan", engine.applyRules("Dikaw i demit a iwina", RuleTranslationDirection.BULOS_TO_FILIPINO))

        assertEquals("Dēku i kotse a oyo", engine.applyRules("Dēku i kotse a oyo", RuleTranslationDirection.BULOS_TO_ENGLISH))
        assertEquals("Dikitam i silpon a oyo", engine.applyRules("Dikitam i silpon a oyo", RuleTranslationDirection.BULOS_TO_ENGLISH))
    }

    @Test
    fun `production rule IDs are unique`() {
        BulosGrammarRuleLoader.loadRules()
        val allRules = BulosGrammarRuleRegistry.getAllRules()
        val ids = allRules.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
