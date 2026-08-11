package com.example.bulosfrontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class OpenerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulosFrontEndTheme {
                OpenerScreen { destination, dialogue ->
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("target_destination", destination)
                        putExtra("dialogue1", dialogue)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun OpenerScreen(onNavigate: (String, String) -> Unit) {
    Scaffold(
        topBar = { BulosTopAppBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { onNavigate("HOME", "english") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                Text(
                    text = stringResource(R.string.choose_lang_english),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onNavigate("HOME", "filipino") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                Text(
                    text = stringResource(R.string.choose_lang_filipino),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onNavigate("HOME", "bulos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                Text(
                    text = stringResource(R.string.choose_lang_bulos),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OpenerScreenPreview() {
    BulosFrontEndTheme {
        OpenerScreen { _, _ -> }
    }
}
