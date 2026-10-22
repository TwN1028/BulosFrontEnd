# GitHub Copilot Instructions: Bulos Dumagat Offline Translation Pipeline

This document provides instructions for GitHub Copilot (and any assistant working on this repository) to ensure full alignment with the Bulos Dumagat offline translation architecture, dataset, and execution order.

## 1. Dataset & Schema
- **Source Dataset**: `Dumagat_Dataset_2026-10-22_22-42.xlsx` (3,021 unique verified rows).
- **Runtime Asset**: `app/src/main/assets/verified_dictionary_additions.json`.
- **Schema Format**: JSON array of objects containing `_category`, `English`, `Filipino`, and `Bulos`.
- **Guardrails**: Do not drop or duplicate rows. Preserve all diacritics (`ù`, `à`, `ē`), orthography notes, synonyms, and placeholders (`___`).

---

## 2. Offline Translation Pipeline (`OfflineDictionary`)
All offline translations must strictly follow this execution order:
1. **Tier 1 (Exact Sentence / Phrase Match)**: Complete normalized sentence or multiword expression match.
2. **Tier 2 (Template / Blank Match)**: Wildcard template matching supporting `___` variable substitution.
3. **Tier 3 (Recursive Chunk / Subdividing Phrase Match)**: Longest-to-shortest n-gram phrase chunk matching for compound sentences.
4. **Tier 4 (Word-by-Word Match)**: Token-by-token exact dictionary lookup with punctuation preservation.
5. **Tier 5 (Pivot-Language Bridge)**: English $\rightarrow$ Filipino $\rightarrow$ Bulos bridging when direct English-to-Bulos matches are partial or missing.
- **Fuzzy Matching**: **Permanently disabled** to prevent hallucinations and word mismatches. Unmatched tokens/sentences correctly fall back to original text (`offline_original_fallback`).
- **Capitalization**: Automatic sentence-initial capitalization preservation (if input starts with an uppercase letter and output starts with a lowercase letter, the output's first character is capitalized).

---

## 3. Grammar Rule Engine (`BulosGrammarRuleEngine`)
- **Structure**: `BulosGrammarRule` data class with ID, direction, description, condition lambda, transformation lambda, priority, and `isEnabled` state.
- **Default State**: All rules **must remain disabled by default** (`isEnabled = false`).
- **Precedence**: Exact dataset matching always takes strict precedence over grammar rules. Rules only apply when exact matching misses and when explicitly enabled and matching the requested translation direction.

---

## 4. Text Normalization (`TextNormalizer`)
- Case-insensitivity (`lowercase(Locale.ROOT)`), whitespace collapse, and trailing sentence punctuation preservation.
- Preserve Unicode diacritics (`ù`, `à`, `ē`) during display cleaning while normalizing for lookups.

---

## 5. Testing & Verification
- Unit tests (`OfflineDictionaryTest`, `VerifiedDictionaryAdditionsTest`, `BulosGrammarRuleEngineTest`) must pass cleanly before any code changes are accepted.
- Refer to `parity_comparison_report.md` for the 50-sentence Filipino-to-Bulos benchmark parity verification.
