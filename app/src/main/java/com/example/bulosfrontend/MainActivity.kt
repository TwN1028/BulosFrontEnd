package com.example.bulosfrontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.bulosfrontend.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val key = intent.getStringExtra("dialogue1") ?: "none"
        val content = DialogueProvider.getDialogue(key)

        setContent {
            BulosFrontEndTheme {
                BulosFrontEndApp(content = content) {
                    startActivity(
                        Intent(this, TranslateTextActivity::class.java).apply {
                            putExtra("dialogue1", key)
                        },
                    )
                }
            }
        }
    }
}

@Preview(name = "Phone", device = Devices.PIXEL_7, showSystemUi = true, showBackground = true)
@Composable
fun BulosFrontEndApp(
    content: DialogueContent = DialogueProvider.getDialogue("preview"),
    onNavigateToTranslate: () -> Unit = {}
) {
    Scaffold(
        topBar = { BulosTopAppBar() },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MainButtonsGrid(
                content = content,
                onNavigateToTranslate = onNavigateToTranslate,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = stringResource(content.footerRes),
                modifier = Modifier.padding(bottom = 16.dp).alpha(0.7f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Light,
                    fontFamily = Aileron,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun MainButtonsGrid(
    content: DialogueContent,
    onNavigateToTranslate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttons = listOf(content.button1Res, content.button2Res, content.button3Res, content.button4Res)
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        buttons.chunked(2).forEachIndexed { index, row ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                row.forEachIndexed { rowIndex, res ->
                    SquareButton(
                        text = stringResource(res),
                        onClick = { if ((index == 0) && (rowIndex == 0)) onNavigateToTranslate() }
                    )
                }
            }
            if (index == 0) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SquareButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(160.dp).aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulosTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_header_title)) },
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_logo_content_description),
                modifier = Modifier.size(48.dp).padding(8.dp),
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
fun MainButtonsGridPreview() {
    BulosFrontEndTheme {
        MainButtonsGrid(content = DialogueProvider.getDialogue("preview"), onNavigateToTranslate = {})
    }
}
