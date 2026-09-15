# Implementation Plan: Revert to Modern UI with Functional Vosk/Dictionary Preservation

This plan restores the app's visual and structural design to the "modern ui" commit (`b0d1099`) while surgically preserving the Vosk ASR and spreadsheet-based translation logic.

## User Review Required

> [!IMPORTANT]
> **Structural Revert**: I am restoring the original file organization from the "modern ui" commit. This means screens currently in separate files (like `TextTranslationScreen.kt` and `HistoryScreen.kt`) will be moved back into `Screens.kt` if that's where they were in the modern UI.
>
> **Design Revert**: The "Absolute Design Centralization" I implemented recently will be reverted in favor of the original `Design.kt` constants from the modern UI branch.

## Proposed Changes

### 1. Restore UI/Design Files
- I will use `git checkout b0d1099` to restore the following files to their "modern ui" state:
    - `Design.kt`
    - `HomeScreen.kt`
    - `SpeechResultScreens.kt`
    - `LanguageSelectionScreen.kt`
    - `PreservationIntroScreen.kt`
    - `SettingsScreen.kt`
    - `DictionaryScreen.kt`
    - `SplashScreen.kt`
    - `MainActivity.kt`
    - `ui/theme/` (Color, Type, Theme)
    - `HomeComponents.kt`
    - `AppBottomNavigation.kt`
    - `UIComponents.kt`
    - `Screens.kt`

### 2. Re-Integrate Functional Logic
Once the UI is restored to the "modern ui" look, I will re-add the logic:

#### [MODIFY] [Design.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Design.kt)
- Re-add `LanguageModelMap` for Vosk.

#### [MODIFY] [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt)
- Update `TranslateTextScreen` to call `viewModel.translateText(text)` using the spreadsheet logic.
- Ensure `LanguageSelector` works with the current `UiLanguage` enums (which I'm keeping for functional accuracy).

#### [MODIFY] [SpeechResultScreens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/SpeechResultScreens.kt)
- Re-inject the `LaunchedEffect` and `VoiceMicrophone` callbacks that drive the Vosk ASR.
- Re-inject the "Recognized Text" live feedback logic.

#### [MODIFY] [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt)
- Keep the `DictionaryManager` and `VoskManager` initialization.
- Ensure all logic remains intact.

### 3. Cleanup
- Delete redundant files that didn't exist in `b0d1099`:
    - `TextTranslationScreen.kt`
    - `HistoryScreen.kt`

## Verification Plan

### Automated Tests
- Run `gradle build` to ensure the restored UI compiles with the current functional logic.

### Manual Verification
1.  **UI Check**: Verify the home screen and other screens match the "modern ui" design exactly.
2.  **ASR Check**: Verify voice recording still shows real-time transcription.
3.  **Dictionary Check**: Verify "Translate" still pulls from `dictionary.xlsx` using the 3-tier hierarchy.
4.  **Diagnostic Check**: Verify `DEBUG_DICT` still works.
