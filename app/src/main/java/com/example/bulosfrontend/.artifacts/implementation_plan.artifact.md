# Implementation Plan: Absolute Design Centralization

This plan outlines the final consolidation of all styling, sizing, and typography logic into the `Design.kt` file. This ensures that no UI-related constants remain scattered across the functional codebase.

## User Review Required

> [!IMPORTANT]
> All typography SP values will be moved from `Type.kt` to `Design.kt`. `Type.kt` will then reference these constants.

> [!NOTE]
> Specific values like alpha transparency (`0.5f`) and specialized weights will also be centralized to ensure a perfectly uniform design language.

## Proposed Changes

### [MODIFY] [Design.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Design.kt)
- **Add Typography SP constants**:
    - `FontDisplayLarge = 64.sp`, `FontDisplayMedium = 52.sp`, `FontDisplaySmall = 42.sp`
    - `FontHeadlineLarge = 40.sp`, `FontHeadlineMedium = 34.sp`, `FontHeadlineSmall = 30.sp`
    - `FontTitleLarge = 28.sp`, `FontTitleMedium = 22.sp`, `FontTitleSmall = 18.sp`
    - `FontBodyLarge = 20.sp`, `FontBodyMedium = 18.sp`, `FontBodySmall = 16.sp`
    - `FontLabelLarge = 18.sp`, `FontLabelMedium = 16.sp`, `FontLabelSmall = 14.sp`
- **Add Opacity & Weights**:
    - `AlphaSecondary = 0.5f`, `AlphaFooter = 0.7f`
- **Add Specialized Sizes**:
    - `IconSizeHistory = 12.dp`

### [MODIFY] [ui/theme/Type.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/ui/theme/Type.kt)
- Update the `Typography` object to use constants from `Design.kt` (e.g., `fontSize = Design.FontBodyLarge`).

### [MODIFY] [UIComponents.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/UIComponents.kt)
- Replace hardcoded `0.7f` alpha in `StandardFooter` with `Design.AlphaFooter`.

### [MODIFY] [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt)
- Replace hardcoded `0.5f` alpha in `Card` colors with `Design.AlphaSecondary`.
- Replace hardcoded `12.dp` in `HistoryCard` icon size with `Design.IconSizeHistory`.
- Ensure all alignments and arrangements use `Design` helpers where possible.

## Verification Plan

### Automated Tests
- Run `gradle assembleDebug` to ensure all cross-file references are resolved correctly.

### Manual Verification
- Verify that the app's visual appearance remains unchanged, but all design changes can now be controlled exclusively via `Design.kt`.
