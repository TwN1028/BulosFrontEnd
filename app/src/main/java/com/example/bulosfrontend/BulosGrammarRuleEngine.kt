package com.example.bulosfrontend

enum class RuleTranslationDirection {
    ENGLISH_TO_BULOS,
    FILIPINO_TO_BULOS,
    BULOS_TO_ENGLISH,
    BULOS_TO_FILIPINO,
    ANY
}

data class BulosGrammarRule(
    val id: String,
    val direction: RuleTranslationDirection,
    val description: String,
    val condition: (String) -> Boolean,
    val transformation: (String) -> String,
    val priority: Int = 100,
    val isEnabled: Boolean = false,
)

object BulosGrammarRuleRegistry {
    private val rules = mutableMapOf<String, BulosGrammarRule>()
    @Volatile var isGrammarRulesEnabled: Boolean = true

    fun register(rule: BulosGrammarRule) {
        rules[rule.id] = rule
    }

    fun unregister(ruleId: String) {
        rules.remove(ruleId)
    }

    fun getRules(direction: RuleTranslationDirection): List<BulosGrammarRule> {
        if (!isGrammarRulesEnabled) return emptyList()
        return rules.values
            .filter { it.isEnabled && (it.direction == direction || it.direction == RuleTranslationDirection.ANY) }
            .sortedBy { it.priority }
    }

    fun getAllRules(): List<BulosGrammarRule> = rules.values.toList()

    fun setEnabled(ruleId: String, enabled: Boolean) {
        rules[ruleId]?.let { rule ->
            rules[ruleId] = rule.copy(isEnabled = enabled)
        }
    }

    fun setAllRulesEnabled(enabled: Boolean) {
        rules.keys.toList().forEach { id ->
            rules[id]?.let { rule ->
                rules[id] = rule.copy(isEnabled = enabled)
            }
        }
    }

    fun clear() {
        rules.clear()
        isGrammarRulesEnabled = true
    }
}

class BulosGrammarRuleEngine {
    fun applyRules(text: String, direction: RuleTranslationDirection): String {
        val activeRules = BulosGrammarRuleRegistry.getRules(direction)
        var currentText = text
        for (rule in activeRules) {
            if (rule.condition(currentText)) {
                currentText = rule.transformation(currentText)
            }
        }
        return currentText
    }
}
