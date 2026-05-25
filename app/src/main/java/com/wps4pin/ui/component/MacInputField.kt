package com.wps4pin.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * MAC input field with auto-formatting.
 *
 * - User types hex chars, dashes are inserted automatically: 00-15-F2-20-4D-6B
 * - Accepts paste with any separator (: - or none)
 * - Uppercase enforced
 * - Max 17 chars (12 hex + 5 dashes)
 */
@Composable
fun MacInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val formatted = formatMacInput(raw)
            onValueChange(formatted)
        },
        label = { Text("MAC-адрес") },
        placeholder = { Text("00-15-F2-20-4D-6B", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)) },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onPaste) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Вставить из буфера"
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            capitalization = KeyboardCapitalization.Characters,
            autoCorrect = false
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

/**
 * Formats raw input into XX-XX-XX-XX-XX-XX pattern.
 * Strips all non-hex chars, uppercases, inserts dashes after every 2 hex chars.
 * Limits to 12 hex chars (6 octets).
 */
private fun formatMacInput(input: String): String {
    val hexOnly = input.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }
    val limited = hexOnly.take(12)
    return limited.chunked(2).joinToString("-")
}
