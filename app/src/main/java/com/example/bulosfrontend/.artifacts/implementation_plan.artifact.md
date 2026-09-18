# Implementation Plan: Revert Design to "Vosk API" State while Preserving Functionality

The goal is to restore the "EarthlyBrown" design introduced in the "apply Vosk API" commit (specifically identified as `9dedb13`/`da86261`) while retaining all the functional improvements made since then, including working speech processing, persistent history, and server diagnostics.

## User Review Required

> [!IMPORTANT]
> **Design vs. Functionality**: I am treating the "Green" theme and new card layouts as "purely design changes" to be reverted. New functional elements like the **ConnectivityStatusPill** will be retained but styled to match the older "EarthlyBrown" palette.

> [!WARNING]
> **Component Synchronization**: Restoring the old `Design.kt` constants may require updating some functional code that was "cleaned up" to remove those constants. I will ensure all references are correctly mapped.

## Proposed Changes

### 1. Restore Design Foundation

#### [MODIFY] [Design.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Design.kt)
- Restore the `EarthlyBrown`, `ButtonBrown`, `LightForestGreen`, etc., color palette.
- Restore the `ButtonShape`, `CardShape`, and `DashboardShape` definitions.
- Restore the extensive typography and dimension constants.
- **Retain**: Keep the `LanguageModelMap` and amplitude normalization logic needed for Vosk.

#### [MODIFY] [Color.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/ui/theme/Color.kt)
- Revert to the color set present in the Vosk API commit, removing the recently added dark mode overrides and updated green palette.

### 2. Revert UI Layouts to Previous State

#### [MODIFY] [HomeScreen.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/HomeScreen.kt)
- Revert the `HomeScreenContent` to use the `HomeFeature` list and `LazyColumn` layout from `9dedb13`.
- Revert the `HomeVoiceHero` and `HomeFabShortcut` to their previous styles.
- **Preserve**: Keep the `ConnectivityStatusPill` and its integration with the ViewModel's `isOnline` state.

#### [MODIFY] [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt) and [SpeechResultScreens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/SpeechResultScreens.kt)
- Restore the `FeaturePatternHeader` and `TranslationInputCard` styles.
- Revert the `HistoryScreen` and `ResultScreen` layouts to their previous versions.
- **Preserve**: Ensure the persistent `HistoryProvider` flow remains connected to the UI.

#### [MODIFY] [UIComponents.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/UIComponents.kt)
- Revert basic components (Buttons, Cards, Headers) to the "EarthlyBrown" style.

#### [MODIFY] [AppBottomNavigation.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/AppBottomNavigation.kt)
- Revert the navigation bar styling and icons to the previous version.

#### [MODIFY] [res/values/themes.xml](file:///D:/Android_Studio_Projects/app/src/main/res/values/themes.xml) and [res/values/strings.xml](file:///D:/Android_Studio_Projects/app/src/main/res/values/strings.xml)
- Revert theme attributes and string labels to match the previous design state.

### 3. Maintain Functional Logic

#### [KEEP] [MainViewModel.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/MainViewModel.kt)
- All Vosk manager initialization, recording logic, and server status monitoring will be preserved.
- Any references to deleted design constants will be updated to the restored constants.

#### [KEEP] [History.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/History.kt)
- The DataStore-backed persistent repository will remain unchanged.

## Verification Plan

### Automated Tests
- `gradle assembleDebug`: Ensure the project builds without errors after reverting design constants.

### Manual Verification
- **UI Audit**: Verify that the app now uses the "EarthlyBrown" color scheme and the previous card layouts.
- **Functionality Check**:
    - Verify that speech recognition (Vosk) still starts and processes audio.
    - Verify that history entries are still persistent.
    - Verify that the Connectivity Status Pill still reflects the server state.
