package com.example.bulosfrontend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HistoryScreen(vm: MainViewModel, onBack: () -> Unit) {
    Scaffold(topBar = { SharedTopAppBar(stringResource(vm.content.historyHeaderRes)) }) { p ->
        Column(Modifier.fillMaxSize().padding(p), horizontalAlignment = Alignment.CenterHorizontally) {
            if (HistoryProvider.history.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No history yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(Design.PaddingElement), verticalArrangement = Arrangement.spacedBy(Design.PaddingElement)) {
                    items(HistoryProvider.history) { HistoryCard(it) }
                }
            }
            BulosButton(onBack, stringResource(vm.content.goBackRes), Modifier.fillMaxWidth().padding(Design.PaddingElement).height(Design.ButtonHeightSmall), colors = Design.dangerColors())
            StandardFooter(vm.content.footerRes)
        }
    }
}

@Composable
fun HistoryCard(item: HistoryItem) {
    Card(Modifier.fillMaxWidth(), shape = Design.CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(Design.AlphaSecondary))) {
        Column(Modifier.padding(Design.PaddingElement), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${item.sourceLang}: ${item.inputText}", style = MaterialTheme.typography.bodyLarge, fontWeight = Design.FontBold, textAlign = TextAlign.Center)
            Icon(Icons.Default.ArrowDownward, null, Modifier.size(Design.IconSizeSmall).padding(Design.PaddingSmall), tint = MaterialTheme.colorScheme.primary)
            Text("${item.targetLang}: ${item.translatedText}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }
    }
}
