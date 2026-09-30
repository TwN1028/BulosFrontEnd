package com.example.bulosfrontend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.example.bulosfrontend.ui.theme.Aileron

internal val TranslationInputBorderColor = Color(0xFFE5DDD2).copy(alpha = 0.65f)
internal val TranslationInputBorderWidth = 0.5.dp

@Composable
internal fun TranslationInputCard(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    editingEnabled: Boolean = true,
    minimumHeight: Dp = TranslationCardMinHeight,
    textFieldHeight: Dp = 252.dp,
    headerAction: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minimumHeight)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(TranslationInputBorderWidth, TranslationInputBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                headerAction?.invoke()
            }
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(textFieldHeight)
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                enabled = editingEnabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = Aileron,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = if (onSubmit == null) ImeAction.Default else ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit?.invoke() },
                ),
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxSize()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Aileron),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            footer?.let {
                Spacer(Modifier.height(8.dp))
                it()
            }
        }
    }
}
