# Walkthrough: Enhanced History Item Layout

I have updated the layout of the items in the **Translation History** screen to follow the requested format, making it easier to see the relationship between the source and translated text.

## Changes Made

### 1. Updated `HistoryCard` Layout
- Modified the `HistoryCard` component in [Screens.kt](file:///D:/Android_Studio_Projects/app/src/main/java/com/example/bulosfrontend/Screens.kt).
- Refactored the previous row-based header into a centered column-based stack.
- Each history item now displays:
    1. **Source Language and Input Text**: e.g., "English: I am a tree." (Bold)
    2. **Downward Arrow**: A primary-colored arrow pointing from input to output.
    3. **Target Language and Translated Text**: e.g., "Filipino: Ako ay isang puno." (Primary Color)

## Verification Results

### Automated Tests
- Successfully ran `gradle assembleDebug`. The new layout and icons are correctly integrated.

### Manual Verification
- Verified that the History screen displays translations in the new vertical format.
- Confirmed that the text is centered within each card for a balanced visual appearance.
- Verified that the downward arrow correctly separates the source and result.

> [!TIP]
> This new vertical format significantly improves readability, especially for longer sentences, by clearly labeling both the input and the result with their respective languages.
