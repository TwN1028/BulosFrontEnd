# Implementation Plan: Strict 3-Tier Translation Hierarchy

This plan refines the translation engine to strictly follow the requested priority: **Full Sentence > Multi-word Phrases > Individual Words**, ensuring all spreadsheet data is fully utilized.

## Proposed Changes

### 1. Robust Data Aggregation

#### [MODIFY] [DictionaryManager.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/DictionaryManager.kt)
- **Sheet-Independent Mapping**: The current loader already merges all sheets. I will add logging to track which sheets provided how many entries to confirm "FULL" checking.
- **Phrase Categorization**: Improve `preparePhrasesCache` to categorize entries into "Sentences" (large chunks) and "Phrases" (smaller multi-word units).

### 2. Strict Hierarchy Logic

#### [MODIFY] [DictionaryManager.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/DictionaryManager.kt)
Update the `translate` method to follow this sequence:

1.  **Tier 1: Full Sentence Match**
    - Clean the input (trim, lowercase, strip trailing punctuation).
    - Scan the **entire dictionary** (all sheets combined) for a match.
    - If found, re-attach the original trailing punctuation and return immediately.

2.  **Tier 2: Greedy Phrase Matching**
    - If Tier 1 fails, take all dictionary entries with **2 or more words**.
    - Sort them by **word count** (descending) so longer phrases are matched first.
    - Perform a regex-based replacement with placeholders to protect translated chunks.

3.  **Tier 3: Word-by-Word Fallback**
    - Split the remaining text into single words.
    - Look up each word in the **entire dictionary**.
    - Preserve punctuation for each word.
    - Keep original if no match is found.

### 3. Diagnostic Transparency

#### [MODIFY] [DictionaryManager.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/DictionaryManager.kt)
- Update `DEBUG_DICT` to report:
    - Total sheets loaded.
    - Row count breakdown (total merged).
    - Confirmation that all tiers are active.

## Verification Plan

### Automated Tests
- Run `gradle build` to verify logic health.

### Manual Verification
1.  **Full Sentence Test**: Spreadsheet has "How are you today?". Input "How are you today?". Verify it matches the full sentence entry.
2.  **Phrase Priority Test**:
    - Sheet 1 has "Good morning".
    - Sheet 2 has "morning" -> "umaga".
    - Input: "He said Good morning to me".
    - Expected: "He said [Translated Phrase] to me" (Phrase match takes priority over individual "morning").
3.  **Punctuation Test**: Input "WHERE IS THE SCHOOL?". Verify Tier 1 matches even with the question mark if the sheet entry is just "where is the school".
