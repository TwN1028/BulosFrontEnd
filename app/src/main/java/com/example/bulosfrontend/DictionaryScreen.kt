package com.example.bulosfrontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.bulosfrontend.ui.theme.ForestGreen
import com.example.bulosfrontend.ui.theme.HomeCardBorder
import com.example.bulosfrontend.ui.theme.WarmWhiteCard

internal fun DictionaryEntry.value(language: UiLanguage): String = when (language) {
    UiLanguage.BULOS -> bulos
    UiLanguage.FILIPINO -> filipino
    UiLanguage.ENGLISH -> english
}

internal fun dictionaryTargetOptions(source: UiLanguage): List<UiLanguage> =
    UiLanguage.entries.filter { target ->
        TranslationLanguageRules.isValidPair(
            TranslationLanguageRules.languageForUi(source),
            TranslationLanguageRules.languageForUi(target),
        )
    }

internal fun dictionaryResults(
    entries: List<DictionaryEntry>,
    source: UiLanguage,
    query: String,
): List<DictionaryEntry> {
    val normalizedQuery = TextNormalizer.normalizeForMatching(query)
    val alphabetical = compareBy<DictionaryEntry> {
        it.value(source).isBlank()
    }.thenBy {
        TextNormalizer.normalizeForMatching(it.value(source))
    }.thenBy {
        it.value(source)
    }
    if (normalizedQuery.isEmpty()) return entries.sortedWith(alphabetical)

    return entries.mapNotNull { entry ->
        val normalizedSource = TextNormalizer.normalizeForMatching(entry.value(source))
        val rank = when {
            normalizedSource == normalizedQuery -> 0
            normalizedSource.startsWith(normalizedQuery) -> 1
            normalizedSource.contains(normalizedQuery) -> 2
            else -> return@mapNotNull null
        }
        rank to entry
    }.sortedWith(
        compareBy<Pair<Int, DictionaryEntry>> { it.first }
            .thenBy { TextNormalizer.normalizeForMatching(it.second.value(source)) }
            .thenBy { it.second.value(source) },
    ).map { it.second }
}

@Composable
fun DictionaryScreen(viewModel: MainViewModel) {
    val home = viewModel.content.home
    val entries = viewModel.dictionaryEntries
    val initialSource = remember {
        UiLanguage.entries.firstOrNull {
            TranslationLanguageRules.languageForUi(it) == TranslationState.sourceLanguage
        } ?: UiLanguage.ENGLISH
    }
    val initialTargetOptions = remember(initialSource) { dictionaryTargetOptions(initialSource) }
    var sourceLanguage by remember { mutableStateOf(initialSource) }
    var targetLanguage by remember {
        mutableStateOf(
            UiLanguage.entries.firstOrNull {
                TranslationLanguageRules.languageForUi(it) == TranslationState.targetLanguage &&
                    it in initialTargetOptions
            } ?: initialTargetOptions.first(),
        )
    }
    var lastBulosTarget by remember {
        mutableStateOf(targetLanguage.takeIf { it in dictionaryTargetOptions(UiLanguage.BULOS) } ?: UiLanguage.ENGLISH)
    }
    var query by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<DictionaryEntry?>(null) }
    val filteredEntries = remember(query, entries, sourceLanguage, targetLanguage) {
        dictionaryResults(entries, sourceLanguage, query)
    }

    selectedEntry?.let { entry ->
        DictionaryEntryDetail(entry = entry, onDismiss = { selectedEntry = null })
    }

    Surface(Modifier.fillMaxSize(), color = HomeContentCream) {
        OverlappingHeaderLayout(
            // Raise the search controls farther into the green header while
            // retaining the same responsive shared-header layout.
            overlap = 48.dp,
            modifier = Modifier.fillMaxSize(),
            header = {
                SharedTopAppBar(
                    title = stringResource(home.dictionaryTitleRes),
                    logoDescriptionRes = home.appLogoDescriptionRes,
                    iconRes = R.drawable.ic_dictionary_book,
                    subtitle = stringResource(home.dictionarySubtitleRes),
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        placeholder = { Text(stringResource(home.dictionarySearchRes)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(home.dictionarySearchDescriptionRes),
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WarmWhiteCard,
                            unfocusedContainerColor = WarmWhiteCard,
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = HomeCardBorder,
                        ),
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DictionaryLanguageSelector(
                            label = stringResource(R.string.dictionary_search_in),
                            selected = sourceLanguage,
                            options = UiLanguage.entries,
                            onSelected = { selectedSource ->
                                if (sourceLanguage == UiLanguage.BULOS && targetLanguage in dictionaryTargetOptions(sourceLanguage)) {
                                    lastBulosTarget = targetLanguage
                                }
                                sourceLanguage = selectedSource
                                val targets = dictionaryTargetOptions(selectedSource)
                                targetLanguage = targetLanguage.takeIf { it in targets }
                                    ?: lastBulosTarget.takeIf { it in targets }
                                    ?: targets.first()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DictionaryLanguageSelector(
                            label = stringResource(R.string.dictionary_show_translation_in),
                            selected = targetLanguage,
                            options = dictionaryTargetOptions(sourceLanguage),
                            onSelected = {
                                targetLanguage = it
                                if (sourceLanguage == UiLanguage.BULOS) lastBulosTarget = it
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (filteredEntries.isEmpty()) {
                Text(
                    text = stringResource(
                        if (query.isBlank()) home.dictionaryEmptyRes else home.dictionaryNoResultsRes,
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(filteredEntries) { _, entry ->
                        DictionaryEntryCard(
                            entry = entry,
                            source = sourceLanguage,
                            target = targetLanguage,
                            onClick = { selectedEntry = entry },
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun DictionaryLanguageSelector(
    label: String,
    selected: UiLanguage,
    options: List<UiLanguage>,
    onSelected: (UiLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier.heightIn(min = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, HomeCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = WarmWhiteCard,
                    contentColor = ForestGreen,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Text(stringResource(selected.displayNameRes), fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(stringResource(language.displayNameRes)) },
                        onClick = {
                            onSelected(language)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DictionaryEntryCard(
    entry: DictionaryEntry,
    source: UiLanguage,
    target: UiLanguage,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LabeledDictionaryValue(source, entry.value(source), primary = true)
            LabeledDictionaryValue(target, entry.value(target), primary = false)
        }
    }
}

@Composable
private fun LabeledDictionaryValue(language: UiLanguage, value: String, primary: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(language.displayNameRes),
            color = ForestGreen,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = value.ifBlank { stringResource(R.string.dictionary_translation_unavailable) },
            style = if (primary) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun DictionaryEntryDetail(entry: DictionaryEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomeContentCream,
        title = { Text(stringResource(R.string.dictionary_entry_details), color = ForestGreen) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(UiLanguage.BULOS, UiLanguage.FILIPINO, UiLanguage.ENGLISH).forEach { language ->
                    LabeledDictionaryValue(language, entry.value(language), primary = false)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dictionary_close)) }
        },
    )
}
