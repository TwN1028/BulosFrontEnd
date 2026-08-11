package com.example.bulosfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

class TranslateTextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val key = intent.getStringExtra("dialogue1") ?: "none"
        val content = DialogueProvider.getDialogue(key)
        setContent {
            BulosFrontEndTheme {
                TranslateTextScreen(content)
            }
        }
    }
}

@Composable
fun TranslateTextScreen(content: DialogueContent) {
    var textState by remember { mutableStateOf("") }
    val maxChar = 100

    Scaffold(
        topBar = { BulosTopAppBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(32.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LanguageSelector(
                    modifier = Modifier.weight(1f),
                    selectedLanguage = TranslationState.sourceLanguage,
                    onLanguageSelected = { TranslationState.sourceLanguage = it },
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                LanguageSelector(
                    modifier = Modifier.weight(1f),
                    selectedLanguage = TranslationState.targetLanguage,
                    onLanguageSelected = { TranslationState.targetLanguage = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = textState,
                onValueChange = { if (it.length <= maxChar) textState = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text(stringResource(content.placeholderRes)) },
                supportingText = {
                    Text(
                        text = "${textState.length} / $maxChar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    TranslationState.textToTranslate = textState
                    // Here you can trigger the server send logic using all TranslationState fields
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(content.translateActionRes),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(value = false) }
    val languages = listOf(
        stringResource(R.string.lang_label_english),
        stringResource(R.string.lang_label_filipino),
        stringResource(R.string.lang_label_bulos)
    )

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = selectedLanguage,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.4f)
        ) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang) },
                    onClick = {
                        onLanguageSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TranslateTextScreenPreview() {
    BulosFrontEndTheme {
        TranslateTextScreen(DialogueProvider.getDialogue("preview"))
    }
}
