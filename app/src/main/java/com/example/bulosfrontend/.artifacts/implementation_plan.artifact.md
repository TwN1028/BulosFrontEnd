# Implementation Plan: Decoupling Translation State

This plan outlines moving the translation-related state variables from `MainViewModel` into a dedicated `TranslationState` object in its own file. This facilitates easier integration for custom translation APIs that may need global or direct access to these parameters.

## User Review Required

> [!NOTE]
> We are reverting to a singleton `object` for translation state (source/target languages, text, and audio paths) while keeping the UI architecture within the `MainViewModel`. This bridges the gap for the custom API integration.

## Proposed Changes

### [NEW] [TranslationState.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/TranslationState.kt)
- Re-introduce the `TranslationState` object.
- Define `sourceLanguage`, `targetLanguage`, `textToTranslate`, `translatedText`, and `recordedAudioPath` using Compose `mutableStateOf`.

### [MODIFY] [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt)
- Remove the translation state properties.
- Update methods (`translateText`, `stopRecording`, etc.) to update `TranslationState` directly.

### [MODIFY] [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt)
- Update UI Composables to read and write state from `TranslationState` instead of the `ViewModel` for the decoupled fields.

## Verification Plan

### Automated Tests
- Run `gradle build` to ensure project compiles.

### Manual Verification
- Deploy to device/emulator.
- Verify that selecting languages, entering text, and recording voice still correctly update the UI and result screens.
