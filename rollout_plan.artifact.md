# Controlled Rollout Plan & Parity Checklist for Android Offline Grammar Rules

## 1. Executive Summary & Rollout Policy
- **Default-Disabled Principle**: All offline grammar rules (`D1–D4`, `B1–B4`, `C2–C6`, `C1–C6`, `E1–E3`, `F1–F5`, `G1–G3`, and validated examples) remain strictly **disabled by default** (`isEnabled = false`). Global grammar rule processing is off by default.
- **Precedence Rule**: Exact dataset full-sentence/phrase matches always take strict precedence over grammar rules and fuzzy matching.
- **Explicit Opt-In**: Rules may only be enabled on a case-by-case basis after native speaker validation and successful regression test verification.

---

## 2. Grammar Rule Rollout Status & Inventory Report

| Backend Rule ID | Direction | Android Implementation Match | Test Coverage | Dataset / Validated Evidence | Known Collisions / Risks | Approval Status |
|---|---|---|---|---|---|---|
| **D1–D4** (Questions) | `bul_to_tl` / `bul_to_en` | Yes (Engine pattern matching) | Covered by `BulosGrammarRuleEngineTest` | Attested in `WORDS` & `PHRASES - SENTENCES` sheets | None with exact matches; potential collision with single-word queries if unconstrained | **Pending Native Speaker Review** (Disabled) |
| **B1–B4** (Possessives) | `bul_to_tl` | Yes (Scoped pattern matcher) | Covered by engine tests | Attested in `PRONOUNS` & `TEMPLATES` sheets | Potential overlap with generic noun linking | **Pending Native Speaker Review** (Disabled) |
| **C2–C6** (Markers/Linkers) | `bul_to_tl` / `bul_to_en` | Yes (Condition transforms) | Covered by engine tests | Attested in `GRAMMAR EVIDENCE` (`G01`–`G07`) | Context sensitivity with `ni` and `nun` | **Pending Native Speaker Review** (Disabled) |
| **C1–C6** (Orthography Guards) | `bul_to_en` | Yes (Identity guardrails) | Covered by engine tests | Attested in `ORTHOGRAPHY NOTES` (`G11`–`G18`) | May short-circuit translation if applied prematurely | **Pending Native Speaker Review** (Disabled) |
| **E1–E3** (Imperatives) | `bul_to_tl` / `bul_to_en` | Yes (Command frames) | Covered by engine tests | Attested in `COMMANDS & REQUESTS` | Overlap with standard verb phrase decomposition | **Pending Native Speaker Review** (Disabled) |
| **F1–F5** (Giving / Transfer) | `bul_to_tl` / `bul_to_en` | Yes (Giving frames) | Covered by engine tests | Attested in `VERBS` & `DATA` | Frame confusion if pronouns are omitted | **Pending Native Speaker Review** (Disabled) |
| **G1–G3** (Verbs / Quantity) | `bul_to_tl` / `bul_to_en` | Yes (Aspect/tense patterns) | Covered by engine tests | Attested in `VERBS` & `DATA` | Tense inference overgeneralization | **Pending Native Speaker Review** (Disabled) |
| **Validated Examples (20)** | `bul_to_tl` / `bul_to_en` | Yes (Exact match rules) | Covered by parity tests | Verified against `Dumagat_Dataset` | None (strict string match) | **Pending Native Speaker Review** (Disabled) |

---

## 3. Re-runnable Parity Checklist
Execute this checklist whenever the backend updates its rule registry, rule implementation, dataset, or benchmark behavior:

- [ ] **Step 1: Dataset Sync**
  - Verify that `verified_dictionary_additions.json` matches the latest backend source Excel workbook (`Dumagat_Dataset_...xlsx`).
  - Run dataset integrity tests (`VerifiedDictionaryAdditionsTest`).
- [ ] **Step 2: Benchmark Parity Verification**
  - Run the 50-sentence Filipino-to-Bulos benchmark fixture (`parity_comparison_report.md`).
  - Confirm 100% exact match or account for any intentional dataset updates.
- [ ] **Step 3: Rule Engine Regression Suite**
  - Execute unit tests (`BulosGrammarRuleEngineTest` and `OfflineDictionaryTest`).
  - Verify that exact full-sentence matching takes precedence over grammar rules.
  - Verify that all rules remain disabled by default (`isEnabled = false`).
- [ ] **Step 4: Build & Release Integrity**
  - Run a clean build (`app:assembleDebug` and `app:testDebugUnitTest`) ensuring **0 errors and 0 warnings**.
