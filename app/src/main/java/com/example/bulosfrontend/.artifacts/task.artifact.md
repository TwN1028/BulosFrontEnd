# Tasks: Bug Cleanup & Redundancy Removal

- `[x]` Consolidate History into Persistent `History.kt`
    - `[x]` Merge DataStore logic into `History.kt`
    - `[x]` Delete `SavedTranslationRepository.kt`
    - `[x]` Update `MainViewModel.kt` to use new `History` logic
    - `[x]` Update UI callers in `Screens.kt` and `SpeechResultScreens.kt`
- `[x]` Prune dead UI constants in `Design.kt`
- `[x]` Resolve linter warnings in `DictionaryManager.kt`
- `[x]` Refactor `MainViewModel` states and clean up imports
- `[x]` Verification & Build
- `[x]` Standardize language codes for API compatibility (fil -> tl)
