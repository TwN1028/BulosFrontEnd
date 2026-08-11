package com.example.bulosfrontend

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
import com.example.bulosfrontend.ui.theme.Aileron
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dialogueKey = intent.getStringExtra("dialogue1") ?: "none"
        val dialogueContent = DialogueProvider.getDialogue(dialogueKey)

        setContent {
            BulosFrontEndTheme {
                BulosFrontEndApp(dialogueContent = dialogueContent)
            }
        }
    }
}

@Preview(name = "Phone", device = Devices.PIXEL_7, showSystemUi = true, showBackground = true)
@Composable
fun BulosFrontEndApp(
    dialogueContent: DialogueContent = DialogueProvider.getDialogue("preview"),
) {
    Scaffold(
        topBar = { BulosTopAppBar() },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MainButtonsGrid(
                content = dialogueContent,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = stringResource(dialogueContent.footerRes),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .alpha(0.7f),
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SquareButton(stringResource(content.button1Res))
            SquareButton(stringResource(content.button2Res))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SquareButton(stringResource(content.button3Res))
            SquareButton(stringResource(content.button4Res))
        }
    }
}

@Composable
fun SquareButton(
    text: String,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { /* TODO */ },
        modifier = modifier
            .size(160.dp)
            .aspectRatio(1f),
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
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
fun MainButtonsGridPreview() {
    BulosFrontEndTheme {
        MainButtonsGrid(content = DialogueProvider.getDialogue("preview"))
    }
}
