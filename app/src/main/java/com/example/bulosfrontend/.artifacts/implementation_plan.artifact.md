# Implementation Plan: Translation History Feature (Decoupled)

This plan outlines the addition of a "Translation History" screen. To ensure easy cleanup and organization, the history storage and logic will be placed in a dedicated file.

## User Review Required

> [!IMPORTANT]
> A new file `History.kt` will be created to manage the history list. `TranslationState` will remain the source of truth for the *active* translation, and entries will be pushed to the history file upon completion.

## Proposed Changes

### Core State & Data Models

#### [NEW] [History.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/History.kt)
- Define `HistoryItem` data class: `sourceLang`, `targetLang`, `inputText`, `translatedText`, `timestamp`.
- Define `HistoryProvider` singleton object with a `mutableStateListOf<HistoryItem>`.
- Add an `addEntry` function to push data from `TranslationState` to the history list.

#### [MODIFY] [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt)
- Update `translateText` and `stopRecording` to call `HistoryProvider.addEntry()` once the translation placeholder (or actual result) is set.

### UI & Navigation

#### [MODIFY] [Dialogue.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Dialogue.kt)
- Add `historyHeaderRes` to `DialogueContent` to handle the title in different languages.

#### [MODIFY] [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt)
- [NEW] Create `HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit)` Composable.
- Use a `LazyColumn` to render items from `HistoryProvider.history`.
- Style items to match the app's Material 3 design (Cards with language labels).

#### [MODIFY] [MainActivity.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainActivity.kt)
- Add the `"history"` route to the `NavHost`.

## Verification Plan

### Automated Tests
- Run `gradle build` to verify compilation.

### Manual Verification
- Perform text and voice translations.
- Navigate to the new "History" screen.
- Verify the list accurately reflects the translations performed.
- Test scrolling and navigation back to the home screen.
