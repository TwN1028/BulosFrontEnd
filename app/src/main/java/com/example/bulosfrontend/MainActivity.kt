package com.example.bulosfrontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val key = intent.getStringExtra("dialogue1") ?: "none"
        val content = DialogueProvider.getDialogue(key)
        setContent {
            BulosFrontEndTheme {
                BulosFrontEndApp(content) { target ->
                    val activity = if (target == "text") TranslateTextActivity::class.java else TranslateVoiceActivity::class.java
                    startActivity(Intent(this, activity).putExtra("dialogue1", key))
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
fun BulosFrontEndApp(content: DialogueContent = DialogueProvider.getDialogue("preview"), onNavigate: (String) -> Unit = {}) {
    Scaffold(topBar = { SharedTopAppBar(stringResource(content.mainHeaderRes)) }) { p ->
        Column(Modifier.padding(p).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            val buttons = listOf(content.button1Res to "text", content.button2Res to "voice", content.button3Res to "history", content.button4Res to "placeholder")
            val chunkedButtons = buttons.chunked(2)
            Column(Modifier.weight(1f).padding(16.dp), Arrangement.Center, Alignment.CenterHorizontally) {
                chunkedButtons.forEachIndexed { index, row ->
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        row.forEach { (res, type) ->
                            SquareButton(stringResource(res)) { if ((type == "text") || (type == "voice")) onNavigate(type) }
                        }
                    }
                    if (index == 0) Spacer(Modifier.height(16.dp))
                }
            }
            StandardFooter(content.footerRes)
        }
    }
}

@Composable
fun SquareButton(text: String, onClick: () -> Unit) = Button(onClick, Modifier.size(160.dp).aspectRatio(1f), shape = RoundedCornerShape(24.dp)) {
    Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
}
