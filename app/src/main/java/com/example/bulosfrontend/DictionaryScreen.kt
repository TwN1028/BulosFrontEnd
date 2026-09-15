package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class DictionaryEntry(val bulos: String, val filipino: String, val english: String)

object DictionaryProvider { val entries: List<DictionaryEntry> = emptyList() }

@Composable
fun DictionaryScreen(viewModel: MainViewModel) {
    val home = viewModel.content.home
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) DictionaryProvider.entries
        else DictionaryProvider.entries.filter { it.bulos.contains(q, true) || it.filipino.contains(q, true) || it.english.contains(q, true) }
    }

    Scaffold(
        topBar = { SharedTopAppBar(stringResource(home.dictionaryTitleRes), home.appLogoDescriptionRes, R.drawable.ic_dictionary_book) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = Design.PaddingElement, vertical = 12.dp),
                placeholder = { Text(stringResource(home.dictionarySearchRes)) },
                leadingIcon = { Icon(Icons.Default.Search, stringResource(home.dictionarySearchDescriptionRes)) },
                singleLine = true,
                shape = Design.CardShape
            )

            if (filtered.isEmpty()) {
                Text(stringResource(if (query.isBlank()) home.dictionaryEmptyRes else home.dictionaryNoResultsRes), Modifier.padding(horizontal = Design.PaddingHorizontal, vertical = Design.PaddingScreen), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = Design.PaddingElement, vertical = Design.PaddingMicro), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered) { DictionaryEntryCard(it) }
                }
            }
        }
    }
}

@Composable
private fun DictionaryEntryCard(entry: DictionaryEntry) = Card(Modifier.fillMaxWidth(), shape = Design.CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Column(Modifier.padding(Design.PaddingElement), verticalArrangement = Arrangement.spacedBy(Design.PaddingMicro)) {
        Text(entry.bulos, style = MaterialTheme.typography.titleMedium, fontWeight = Design.FontBold)
        Text(entry.filipino, style = MaterialTheme.typography.bodyMedium)
        Text(entry.english, style = MaterialTheme.typography.bodyMedium)
    }
}
