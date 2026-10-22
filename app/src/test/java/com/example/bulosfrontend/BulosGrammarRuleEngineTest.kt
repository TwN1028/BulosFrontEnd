package com.example.bulosfrontend

import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `batch 3 possessive rules B1 to B4 transform correctly`() {
        BulosGrammarRuleLoader.loadRules()

        val r1 = engine.applyRules("Dēku i beloy a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa akin ang beloy na ito", r1)

        val r2 = engine.applyRules("Dikaw i silpon", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa iyo ang cellphone", r2)

        val r3 = engine.applyRules("Dikami i demit a iwina", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa amin ang demit na iyan", r3)

        val r4 = engine.applyRules("Diya i bengka", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Sa kaniya ang bengka", r4)
    }

    @Test
    fun `batch 3 possessive rules reject near misses and unsupported nouns`() {
        BulosGrammarRuleLoader.loadRules()

        val nearMiss1 = engine.applyRules("Dēku i kotse a oyo", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Dēku i kotse a oyo", nearMiss1)

        val nearMiss2 = engine.applyRules("Dikaw i beloy a invalid", RuleTranslationDirection.BULOS_TO_FILIPINO)
        assertEquals("Dikaw i beloy a invalid", nearMiss2)
    }
}
