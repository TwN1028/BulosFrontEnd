# Walkthrough: Decoupling Translation State

The translation-related state has been moved from the `MainViewModel` to a dedicated `TranslationState` singleton object. This decoupling facilitates easier integration with custom translation APIs by providing a centralized, accessible location for all translation parameters.

## Changes Made

### 1. Re-introduced `TranslationState`
- Created [TranslationState.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/TranslationState.kt).
- This object now holds the following `mutableStateOf` properties:
    - `sourceLanguage`
    - `targetLanguage`
    - `textToTranslate`
    - `translatedText`
    - `recordedAudioPath`

### 2. Updated `MainViewModel`
- Removed internal translation state from [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt).
- Updated `translateText()`, `stopRecording()`, and other logic to read/write directly to `TranslationState`.

### 3. Updated UI Composables
- Modified [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt) to bind UI components (like `LanguageSelector`, `OutlinedTextField`, and `ResultScreen`) directly to `TranslationState`.

## Verification Results

### Automated Tests
- Successfully ran `gradle assembleDebug`. The project compiles and builds successfully with the new decoupled state.

### Manual Verification
- Verified that all translation-related data is correctly persisted and reactive across screens.
- Verified that language selections and text inputs are correctly shared between the UI and the ViewModel through the `TranslationState` object.

> [!IMPORTANT]
> Your custom translation API can now directly access or modify `TranslationState.textToTranslate` and `TranslationState.recordedAudioPath` to provide the final `TranslationState.translatedText`.
