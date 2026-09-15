package com.example.bulosfrontend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class DictionaryEntry(
    val bulos: String,
    val filipino: String,
    val english: String,
)

object DictionaryProvider {
    // The verified Bulos dataset can be added here without changing the screen.
    val entries: List<DictionaryEntry> = emptyList()
}

@Composable
fun DictionaryScreen(viewModel: MainViewModel) {
    val home = viewModel.content.home
    var query by remember { mutableStateOf("") }
    val filteredEntries = remember(query) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            DictionaryProvider.entries
        } else {
            DictionaryProvider.entries.filter { entry ->
                entry.bulos.contains(normalizedQuery, ignoreCase = true) ||
                    entry.filipino.contains(normalizedQuery, ignoreCase = true) ||
                    entry.english.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = stringResource(home.dictionaryTitleRes),
                logoDescriptionRes = home.appLogoDescriptionRes,
                iconRes = R.drawable.ic_dictionary_book,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text(stringResource(home.dictionarySearchRes)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(home.dictionarySearchDescriptionRes),
                    )
                },
                singleLine = true,
            )

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
                    items(filteredEntries) { entry ->
                        DictionaryEntryCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun DictionaryEntryCard(entry: DictionaryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(entry.bulos, style = MaterialTheme.typography.titleMedium)
            Text(entry.filipino, style = MaterialTheme.typography.bodyMedium)
            Text(entry.english, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
