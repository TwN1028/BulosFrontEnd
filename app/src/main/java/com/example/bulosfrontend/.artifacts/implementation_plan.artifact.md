# Implementation Plan: Bug Cleanup & Redundancy Removal (Revised)

This plan streamlines the codebase by consolidating history management into a single persistent repository based in `History.kt`, pruning dead UI constants, and resolving linter warnings.

## Proposed Changes

### 1. Consolidate History into Persistent `History.kt`

#### [MODIFY] [History.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/History.kt)
- Integrate the persistence logic currently in `SavedTranslationRepository.kt` into this file.
- Use **Jetpack DataStore** to save and load `HistoryItem` objects.
- Expose a `Flow<List<HistoryItem>>` from a `HistoryRepository` class or the `HistoryProvider` object.

#### [DELETE] [SavedTranslationRepository.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/SavedTranslationRepository.kt)
- Remove this file as its functionality has been merged into `History.kt`.

#### [MODIFY] [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt)
- Update to use the updated `History.kt` logic.
- Remove the dependency on `SavedTranslationRepository`.

#### [MODIFY] [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt) and [SpeechResultScreens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/SpeechResultScreens.kt)
- Ensure UI components correctly observe the new persistent history flow.

### 2. Prune Dead Code & Polish Logic

#### [MODIFY] [Design.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Design.kt)
- Delete all unused colors, typography, dimensions, and layout helpers identified by the IDE analysis.

#### [MODIFY] [DictionaryManager.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/DictionaryManager.kt)
- Simplify Regex for control characters.
- Replace manual lowercase comparisons with `equals(..., ignoreCase = true)`.
- Fix foldable if-then logic for JSON loading.

#### [MODIFY] [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt)
- Clean up unused local variables and imports.
- Add named parameters to boolean `mutableStateOf` calls for clarity.

## User Review Required

> [!IMPORTANT]
> **Consolidation**: By moving the DataStore logic into `History.kt`, we fulfill the requirement to make `History.kt` the primary persistent repository while keeping the codebase organized.

## Verification Plan

### Automated Tests
- Run `gradle assembleDebug` to ensure no compile-time errors after refactoring.
- Re-run `analyze_file` on modified files to verify all warnings are resolved.

### Manual Verification
- **History View**: Open the History screen and verify all previously saved translations are still visible.
- **Save Feature**: Translate a sentence and verify it still saves correctly and appears in the list.
- **ASR/Vosk**: Verify that speech recognition still functions correctly.
