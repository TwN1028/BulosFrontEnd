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
import androidx.compose.ui.unit.dp
import com.example.bulosfrontend.ui.theme.BulosFrontEndTheme

class OpenerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulosFrontEndTheme {
                OpenerScreen { key -> startActivity(Intent(this, MainActivity::class.java).putExtra("dialogue1", key)) }
            }
        }
    }
}

@Composable
fun OpenerScreen(onNavigate: (String) -> Unit) {
    val options = listOf("english" to R.string.choose_lang_eng, "filipino" to R.string.choose_lang_fil, "bulos" to R.string.choose_lang_bul)
    Scaffold(topBar = { SharedTopAppBar(stringResource(R.string.header_main_eng)) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(horizontal = 32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            options.forEach { (key, res) ->
                Button({ onNavigate(key) }, Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(res), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                }
                if (key != options.last().first) Spacer(Modifier.height(24.dp))
            }
        }
    }
}
