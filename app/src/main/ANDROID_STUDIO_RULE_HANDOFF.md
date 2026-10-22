# Android Studio Agent: Bulos backend rule handoff

Use this document to port the backend's existing rule behavior to the Android
application in small, testable batches. The backend and Android project use the
same dataset, but the rules below are application code and are not generated
automatically from the workbook.

## Important status and integration constraints

- The central backend registry contains **74 rules; all 74 are disabled**.
  The global `grammar_rules_enabled` setting also defaults to `false`.
- `grammar_rules/validated_examples.py` contains **20 additional opt-in
  examples** (18 exact examples and 2 constrained possessive patterns). They
  are not part of the central registry. `ACTIVE_RULES` is a test/opt-in copy
  with those rules enabled; it does not mean they are active in normal service
  requests.
- Registered grammar rules currently cover only `bul_to_tl` and `bul_to_en`.
  There are **no rules** in the `word_order` or `negation` modules.
- The English-to-Bulos route first checks an exact full-sentence dataset match.
  On a miss, it pivots the entire English input through English-to-Filipino,
  then runs the local Filipino-to-Bulos translator. It does not invoke the
  grammar-rule engine. Do not assume enabling a grammar flag makes these rules
  run on English-to-Bulos pivot output.
- In normal applicable directions, exact full-text dataset matching precedes
  grammar rules. The default registry rules are currently disabled, so the
  regular phrase/word/fuzzy pipeline continues unless a rule is explicitly
  enabled.
- Rules have an ID, direction, description, condition, transform, priority,
  enabled flag, and application-confidence metadata. Application confidence
  is **not translation accuracy**.
- Rule engine behavior: consider enabled rules for the requested direction;
  collect all matching conditions; use the unique highest-priority match;
  if highest-priority rules tie, return the original text with a conflict
  diagnostic; if none match, return the original text so the normal translation
  pipeline can continue.
- Most fixed-pattern rules use case-insensitive whole-string matching after
  trimming outer whitespace. The variable possessive rules use constrained
  pattern matching. Orthography guards are identity transforms, not spelling
  converters. Follow each listed input/output exactly; do not generalize a
  pattern into a broader grammar rule.
- Source dataset: `data/Dumagat_Dataset_2026-10-22_03-02.xlsx`; runtime data:
  `dictionary.json` and `sentence.json`. Check that workbook evidence before
  extending patterns.

## Recommended implementation batches

1. Engine semantics and tests: exact lookup precedence, direction filtering,
   disabled-by-default handling, priority conflicts, and diagnostics.
2. Questions: `D1` through `D4`.
3. Pronouns/possessives: `B1` through `B4`.
4. Sentence structure: `D1` through `D3`.
5. Particles/markers: `C2` through `C6`.
6. Imperatives/requests: `E1` through `E3`.
7. Giving/transfer: `F1` through `F5`.
8. Verb/quantity examples: `G1` through `G3`.
9. Orthography guards: `C1` through `C6`, only if the Android pipeline can
   preserve the matched source unchanged without interfering with ordinary
   translation.
10. Optional exact examples and possessive candidates from the separate
    `validated_examples` inventory. Keep these distinct from the default
    registry unless explicitly opted in.

Implement and test each batch against both directions where present. Do not
enable all batches globally merely because they have been ported.

## Batch 2: Questions (22 registered rules)

All rules are disabled, priority `200`, application confidence `0.93`.

| Rule ID | Direction | Exact Bulos input -> output |
|---|---|---|
| `D1-ANU-ROOT-TL` | `bul_to_tl` | `Anu?` -> `Ano?` |
| `D1-ANU-ROOT-EN` | `bul_to_en` | `Anu?` -> `What?` |
| `D1-ANU-NAME-TL` | `bul_to_tl` | `Anu i ngalan mu?` -> `Ano ang pangalan mo?` |
| `D1-ANU-NAME-EN` | `bul_to_en` | `Anu i ngalan mu?` -> `What is your name?` |
| `D1-ANU-FOOD-TL` | `bul_to_tl` | `Anu a papangan i pepokaluto mu?` -> `Anong pagkain ang niluluto mo?` |
| `D1-ANU-FOOD-EN` | `bul_to_en` | `Anu a papangan i pepokaluto mu?` -> `What food are you preparing?` |
| `D2-DENU-ROOT-TL` | `bul_to_tl` | `Denu?` -> `Saan?` |
| `D2-DENU-ROOT-EN` | `bul_to_en` | `Denu?` -> `Where?` |
| `D2-DENU-PLACE-TL` | `bul_to_tl` | `Denu un tanuman?` -> `Nasaan ang halamanan?` |
| `D2-DENU-PLACE-EN` | `bul_to_en` | `Denu un tanuman?` -> `Where is the garden?` |
| `D2-DENO-ORIGIN-TL` | `bul_to_tl` | `Tage deno ka?` or `Tage dēno ka?` -> `Taga saan ka?` |
| `D2-DENO-ORIGIN-EN` | `bul_to_en` | `Tage deno ka?` or `Tage dēno ka?` -> `Where are you from?` |
| `D3-KAPIDE-ROOT-TL` | `bul_to_tl` | `Kapide?` -> `Kailan?` |
| `D3-KAPIDE-ROOT-EN` | `bul_to_en` | `Kapide?` -> `When?` |
| `D3-KAPIDE-TIME-TL` | `bul_to_tl` | `Kapide ka dumalong?` -> `Kailan ka dumating?` |
| `D3-KAPIDE-TIME-EN` | `bul_to_en` | `Kapide ka dumalong?` -> `When did you come?` |
| `D4-PIDE-ROOT-TL` | `bul_to_tl` | `Pide? Pide?` -> `Magkano? Ilan?` |
| `D4-PIDE-ROOT-EN` | `bul_to_en` | `Pide? Pide?` -> `How much? / How many?` |
| `D4-PIDE-PRICE-TL` | `bul_to_tl` | `Pide i oyo?` -> `Magkano ito?` |
| `D4-PIDE-PRICE-EN` | `bul_to_en` | `Pide i oyo?` -> `How much is it?` |
| `D4-PIDE-QUANTITY-TL` | `bul_to_tl` | `Pide un aduwa?` -> `Magkano ang dalawa?` |
| `D4-PIDE-QUANTITY-EN` | `bul_to_en` | `Pide un aduwa?` -> `How much for two?` |

These are context-specific examples. In particular, do not treat `anu`, `denu`,
`kapide`, or `pide` as universal substitutions independent of the full pattern.

## Batch 3: Pronoun and possessive patterns (4 registered rules)

All rules are disabled, priority `200`, application confidence `0.82`,
direction `bul_to_tl`. The shared matcher requires a supported pronoun followed
by `i` and an explicitly supported noun. It optionally accepts `a` plus a
supported demonstrative.

Supported nouns: `silpon` -> `cellphone`; `demit` -> `damit`; `bengka` ->
`bangka`; `beloy` -> `bahay`.

Supported demonstratives: `oyo` -> `ito`; `iwina` -> `iyan`.

| Rule ID | Bulos source pattern | Filipino result |
|---|---|---|
| `B1-POSSESSIVE-DEKU` | `Dēku i <noun>` optionally `a <demo>` | `Sa akin ang <noun>` optionally `na <demo>` |
| `B2-POSSESSIVE-DIKAW` | `Dikaw i <noun>` optionally `a <demo>` | `Sa iyo ang <noun>` optionally `na <demo>` |
| `B3-POSSESSIVE-DIKAMI` | `Dikami i <noun>` optionally `a <demo>` | `Sa amin ang <noun>` optionally `na <demo>` |
| `B4-POSSESSIVE-DIYA` | `Diya i <noun>` optionally `a <demo>` | `Sa kaniya ang <noun>` optionally `na <demo>` |

Only those four nouns and two demonstratives are accepted. Preserve diacritics
in the `Dēku` spelling; do not normalize it to a different form.

## Batch 4: Sentence structure (10 registered rules)

All rules are disabled, priority `180`, application confidence `0.86`.

| Rule ID | Direction | Exact Bulos input -> output |
|---|---|---|
| `D1-DESCRIPTOR-BELOY-TL` | `bul_to_tl` | `Hanga i beloy ni patod` -> `Malaki ang bahay ng lalaki` |
| `D1-DESCRIPTOR-BELOY-EN` | `bul_to_en` | `Hanga i beloy ni patod` -> `The house of man is big` |
| `D1-DESCRIPTOR-SAMOY-TL` | `bul_to_tl` | `Mokpal i samoy ni Masampat` -> `Makapal ang buhok ni Masampat` |
| `D1-DESCRIPTOR-SAMOY-EN` | `bul_to_en` | `Mokpal i samoy ni Masampat` -> `Masampat has thick hair` |
| `D2-LOCATIVE-POSSESSION-DIKITAM-TL` | `bul_to_tl` | `Dikitam i beloy de Tumagtag` -> `Sa atin ang bahay sa Tumagtag` |
| `D2-LOCATIVE-POSSESSION-DIKITAM-EN` | `bul_to_en` | `Dikitam i beloy de Tumagtag` -> `The house in Tumagtag is ours` |
| `D2-LOCATIVE-POSSESSION-DIKAMU-TL` | `bul_to_tl` | `Dikamu i beloy de Sinilaben` -> `Sa inyo ang bahay sa Sinilaban` |
| `D2-LOCATIVE-POSSESSION-DIKAMU-EN` | `bul_to_en` | `Dikamu i beloy de Sinilaben` -> `The house in Sinilaban is yours` |
| `D3-POSSESSIVE-PLURAL-DIDE-TL` | `bul_to_tl` | `Didē i beloy a inon` -> `Sa kanila ang bahay na iyon` |
| `D3-POSSESSIVE-PLURAL-DIDE-EN` | `bul_to_en` | `Didē i beloy a inon` -> `That house is theirs` |

The regex permits optional trailing whitespace on the `D2` and `D3` examples;
the `D1` patterns do not permit a final period.

## Batch 5: Particles and markers (10 registered rules)

All rules are disabled, priority `180`, application confidence `0.88`.

| Rule ID | Direction | Bulos input -> output |
|---|---|---|
| `C2-NI-RELATIONAL-NAME-TL` | `bul_to_tl` | `ngalan ni agla` -> `pangalan ng tao` |
| `C2-NI-RELATIONAL-NAME-EN` | `bul_to_en` | `ngalan ni agla` -> `name of person` |
| `C3-NI-VS-NUN-NEAR-TL` | `bul_to_tl` | `Pegemit i ni bege i petokuy a ge surot ay de alani diya.` (period optional) -> `Ginagamit ang ni kung ang tinutukoy ng nagsasalita ay malapit sa kanya` |
| `C3-NI-VS-NUN-FAR-TL` | `bul_to_tl` | `Pegemit i nun bege i petokuy a ge surot ay de alayu diya.` (period optional) -> `Ginagamit ang nun kung ang tinutukoy ng nagsasalita ay malayo sa kanya` |
| `C4-DE-LOCATION-TL` | `bul_to_tl` | `Noduman i eya de tindehan` (period optional) -> `Pupunta siya sa tindahan` |
| `C4-DE-LOCATION-EN` | `bul_to_en` | `Noduman i eya de tindehan` (period optional) -> `He is going to the store` |
| `C5-I-AS-TITLE-ARTICLE-TL` | `bul_to_tl` | `I Pamamaraan ni Pagsolat ni Surot a Dumaget Bulos` (period optional) -> `Ang Pamamaraan ng Pagsulat sa Wikang Dumaget Bulos` |
| `C5-I-AS-TITLE-ARTICLE-EN` | `bul_to_en` | same source as above -> `Introduction to Dumaget Bulos Orthography` |
| `C6-A-LINKER-DEMONSTRATIVE-TL` | `bul_to_tl` | `Binoy děku i alit a oyo` (accepts `dēku`, `deku`, `děku`; period optional) -> `Ibinigay sa akin ang damit na ito` |
| `C6-A-LINKER-DEMONSTRATIVE-EN` | `bul_to_en` | same source variants -> `This dress is given to me` |

The `C3` distinction is sentence-context-specific; do not implement `ni` and
`nun` as universal near/far replacements. The title rule applies to the full
title only, not every occurrence of `i`.

## Batch 6: Imperatives and requests (6 registered rules)

All rules are disabled, priority `180`, application confidence `0.88`. Final
period is optional in each source pattern.

| Rule ID | Direction | Bulos input -> output |
|---|---|---|
| `E1-IMPERATIVE-MO-TL` | `bul_to_tl` | `Lenisin mo i beloy.` -> `Linisin mo ang bahay` |
| `E1-IMPERATIVE-MO-EN` | `bul_to_en` | same source -> `Clean the house` |
| `E2-IMPERATIVE-YU-TL` | `bul_to_tl` | `Tapusin yu i yedein.` -> `Tapusin ninyo ang gawain` |
| `E2-IMPERATIVE-YU-EN` | `bul_to_en` | same source -> `Finish your work` |
| `E3-IMPERATIVE-TAM-TL` | `bul_to_tl` | `Amitin tam un ikan.` -> `Kuhanin natin ang isda` |
| `E3-IMPERATIVE-TAM-EN` | `bul_to_en` | same source -> `Let us get the fish` |

## Batch 7: Giving and transfer (10 registered rules)

All rules are disabled, priority `180`, application confidence `0.88`. Final
period is optional in each source pattern.

| Rule ID | Direction | Bulos input -> output |
|---|---|---|
| `F1-GIVE-MO-ITEM-TL` | `bul_to_tl` | `Biyen mo i eya ni ikan.` -> `Bigyan mo siya ng isda` |
| `F1-GIVE-MO-ITEM-EN` | `bul_to_en` | same source -> `Give him/her fish` |
| `F2-GIVE-TAM-TO-CHILDREN-TL` | `bul_to_tl` | `Biyen tam ni sinaing un manganak.` -> `Bigyan natin ng kanin ang mga bata` |
| `F2-GIVE-TAM-TO-CHILDREN-EN` | `bul_to_en` | same source -> `Let us give rice to the children` |
| `F3-GIVE-YU-FOOD-TL` | `bul_to_tl` | `Boyin yu un papangan nunde manganak.` -> `Ibigay ninyo ang pagkain sa mga bata` |
| `F3-GIVE-YU-FOOD-EN` | `bul_to_en` | same source -> `Give the foods to the children` |
| `F4-GIVE-RECIPIENT-PLURAL-TL` | `bul_to_tl` | `Binoy mi un kapapelan dide.` -> `Ibinigay namin ang mga papel sa kanila` |
| `F4-GIVE-RECIPIENT-PLURAL-EN` | `bul_to_en` | same source -> `We gave the papers to them` |
| `F5-GIVE-THIRD-PERSON-TL` | `bul_to_tl` | `Binoy na un bokan.` -> `Ibinigay niya ang bulaklak` |
| `F5-GIVE-THIRD-PERSON-EN` | `bul_to_en` | same source -> `He/She gave the flower` |

## Batch 8: Verb and quantity examples (6 registered rules)

All rules are disabled, priority `190`, application confidence `0.90`.

| Rule ID | Direction | Exact Bulos input -> output |
|---|---|---|
| `G1-NAGYEDI-PAST-TL` | `bul_to_tl` | `Nagyedi ok ni beloy/Yumadi ok ni beloy` -> `Gumawa ako ng bahay` |
| `G1-NAGYEDI-PAST-EN` | `bul_to_en` | same source -> `I built house` |
| `G2-GEHANDE-PROGRESSIVE-TL` | `bul_to_tl` | `Gehande ok ni papangan` (period optional) -> `Naghahanda ako ng pagkain` |
| `G2-GEHANDE-PROGRESSIVE-EN` | `bul_to_en` | same source -> `I am preparing foods` |
| `G3-NANGAMITOK-QUANTITY-TL` | `bul_to_tl` | `Nangamitok ni aduwa` (period optional) -> `Kukuha ako ng dalawa` |
| `G3-NANGAMITOK-QUANTITY-EN` | `bul_to_en` | same source -> `I will get two` |

The slash in the first `G1` input is literal source text in the current rule;
do not split it into alternatives. These examples do not justify a universal
tense/aspect rule for `ge-`, `nag-`, or `na-`.

## Batch 9: Orthography safeguards (6 registered identity rules)

All rules are disabled, direction `bul_to_en`, priority `250`, application
confidence `0.90`. Each matching transform returns the original input unchanged.
They are guards, not translation rules.

| Rule ID | Match condition (searches anywhere in input) | Behavior |
|---|---|---|
| `C1-HYPHEN-PRESERVE` | Two letter sequences joined by `-` | Preserve the original string |
| `C2-APOSTROPHE-PRESERVE` | A word containing straight or curly apostrophe | Preserve the original string |
| `C3-DIACRITIC-PRESERVE` | Contains one of the configured Bulos diacritic characters | Preserve the original string |
| `C4-PROPER-NAME-PRESERVE` | Likely capitalized name or all-caps token | Preserve the original string |
| `C5-LOANWORD-PRESERVE` | Pattern that looks like a loanword/camel-case form | Preserve the original string |
| `C6-REPEATED-ROOT-PRESERVE` | Repeated adjacent word or hyphenated form | Preserve the original string |

Since these return unchanged text, integrating them as an early unconditional
translation result would block translation. Preserve these source features
within the normal translator instead; only port an identity guard where it
does not short-circuit the actual translation.

## Separate opt-in inventory: 20 validated examples/candidates

These are defined in `grammar_rules/validated_examples.py`, not in the
74-rule central registry. All 20 are initially disabled. The 18 exact examples
match the complete source string case-sensitively (outer whitespace is
trimmed); their exact punctuation is part of the match. Priorities/confidence:
14 exact rules at priority `210` (application confidence shown below), 4 exact
rules at priority `205` (0.97), and the 2 parameterized candidates at priority
`180` (0.80).

### Exact examples (18)

| Rule ID | Direction | Exact input -> output | Confidence |
|---|---|---|---:|
| `E1-QUESTION-NAME-TL` | `bul_to_tl` | `Anu i ngalan mo?` -> `Ano ang pangalan mo?` | 0.98 |
| `E1-QUESTION-NAME-EN` | `bul_to_en` | `Anu i ngalan mo?` -> `What is your name?` | 0.98 |
| `D2-DEMONSTRATIVE-YOURS-TL` | `bul_to_tl` | `Dikaw i silpon a oyo` -> `Sa iyo ang cellphone na ito` | 0.97 |
| `D2-DEMONSTRATIVE-YOURS-EN` | `bul_to_en` | `Dikaw i silpon a oyo` -> `This cellphone is yours` | 0.97 |
| `D2-DEMONSTRATIVE-MINE-TL` | `bul_to_tl` | `Dēku i silpon a oyo` -> `Sa akin ang cellphone na ito` | 0.97 |
| `D2-DEMONSTRATIVE-MINE-EN` | `bul_to_en` | `Dēku i silpon a oyo` -> `This cellphone is mine` | 0.97 |
| `M2-HAIR-THICK-TL` | `bul_to_tl` | `Mokpal i samoy ni Masampat` -> `Makapal ang buhok ni Masampat` | 0.96 |
| `M2-HAIR-THICK-EN` | `bul_to_en` | `Mokpal i samoy ni Masampat` -> `Masampat has thick hair` | 0.96 |
| `P3-POSSESSIVE-MINE-GARDEN-TL` | `bul_to_tl` | `Dëku un tanuman.` -> `Sa akin ang halamanan.` | 0.97 |
| `P3-POSSESSIVE-MINE-GARDEN-EN` | `bul_to_en` | `Dëku un tanuman.` -> `It is my garden.` | 0.97 |
| `P4-POSSESSIVE-MINE-HOUSE-TL` | `bul_to_tl` | `Beloy ko nay.` -> `Bahay ko iyan.` | 0.97 |
| `P4-POSSESSIVE-MINE-HOUSE-EN` | `bul_to_en` | `Beloy ko nay.` -> `That is my house.` | 0.97 |
| `Q3-WHOSE-HOUSE-TL` | `bul_to_tl` | `Dinu a beloy i wina?` -> `Kaninong bahay iyan?` | 0.97 |
| `Q3-WHOSE-HOUSE-EN` | `bul_to_en` | `Dinu a beloy i wina?` -> `Whose house is that?` | 0.97 |
| `P1-POSSESSIVE-OURS-BANGKA-TL` | `bul_to_tl` | `Dikami i bengka` -> `Sa amin ang bangka` | 0.97 |
| `P1-POSSESSIVE-OURS-BANGKA-EN` | `bul_to_en` | `Dikami i bengka` -> `The boat that is ours` | 0.97 |
| `P2-POSSESSIVE-OURS-HOUSE-TL` | `bul_to_tl` | `Dikami i beloy de Tagpis` -> `Sa amin ang bahay sa Tagpis` | 0.97 |
| `P2-POSSESSIVE-OURS-HOUSE-EN` | `bul_to_en` | `Dikami i beloy de Tagpis` -> `The house in Tagpis is ours` | 0.97 |

### Constrained possessive candidates (2)

| Rule ID | Direction | Match and transformation |
|---|---|---|
| `BULOS-CANDIDATE-POSSESSIVE-EN` | `bul_to_en` | `<pronoun> i <noun> a <demonstrative>` -> `<This/That> <English noun> is <possessive>` |
| `BULOS-CANDIDATE-POSSESSIVE-TL` | `bul_to_tl` | `<pronoun> i <noun> a <demonstrative>` -> `<Filipino possessive> ang <Filipino noun> na <Filipino demonstrative>` |

Allowed pronouns: `dēku` -> mine / Sa akin; `dikaw` -> yours / Sa iyo;
`dikami` -> ours / Sa amin; `diya` -> hers/his / Sa kaniya.
Allowed nouns: `silpon` -> cellphone; `demit` -> dress / damit; `bengka` ->
boat / bangka; `beloy` -> house / bahay. Allowed demonstratives: `oyo` ->
this / ito; `iwina` -> that / iyan. The English candidate matches only this
shape with these enumerated values. The Filipino candidate supports the same
shape and enumerated values. Do not generalize to arbitrary nouns or pronouns.

## Existing non-grammar translation safeguards (no GrammarRule IDs)

These behaviors also need to remain consistent with the shared dataset:

- Dataset exact sentence/phrase matches precede token matching.
- Dictionary/sentence directions are indexed only when both source and target
  values are present; blank, `-`, and em-dash placeholders are not translations.
- Dataset row order determines the first exact target when a source has
  multiple recorded targets.
- Comma-separated single-word Bulos alternatives are separate targets;
  `aku` is preferred over `ok` when the loader is selecting a synonym.
  Other slash-separated source text is not generically treated as synonyms.
- For Filipino-to-Bulos, documented `nasaan` can use the `saan` lookup;
  `dalawang` and `isang` can try `dalawa` and `isa` only when a base mapping
  exists.
- Filipino source forms with a missing Bulos target are protected from fuzzy
  guessing only if no verified Bulos mapping for that normalized source exists.
- In the attested temporal phrase `bago dumating`, preserve `bago`; do not
  replace it using an unrelated “new” word match.
- Numbers pass through word matching unchanged. Proper names, punctuation,
  capitalization, and meaningful diacritics have dedicated handling.
- Fuzzy matching is a fallback, not permission to invent translations; exact
  phrases, protected names/words, and verified unresolved forms take precedence.

## Backend references for parity checks

- `services/grammar_rules.py`: rule type, registry, matching, priority, conflict
  and diagnostic semantics.
- `services/grammar_rule_registry.py`: the 74-rule central composition.
- `grammar_rules/questions.py`, `pronouns.py`, `sentence_structure.py`,
  `particles.py`, `imperatives.py`, `giving.py`, `verb_morphology.py`,
  `orthography.py`: registered rule batches.
- `grammar_rules/validated_examples.py`: separate 20-rule opt-in inventory.
- `services/translation.py`: exact lookup, grammar activation gate, translation
  pipeline ordering, and dataset-backed safeguards.
- `tests/test_grammar_rules.py` and `tests/test_validated_bulos_rules.py`:
  behavioral test expectations.
