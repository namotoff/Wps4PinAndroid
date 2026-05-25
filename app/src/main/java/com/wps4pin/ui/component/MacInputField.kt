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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * MAC input field with auto-formatting via VisualTransformation.
 *
 * - User types hex chars, dashes are shown automatically: 00-15-F2-20-4D-6B
 * - Cursor stays in correct position (no jumping)
 * - Accepts paste with any separator (: - or none)
 * - Uppercase enforced
 * - Max 12 hex chars (6 octets)
 */
@Composable
fun MacInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualTransformation = remember { MacVisualTransformation() }

    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val cleaned = raw.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }
            onValueChange(cleaned.take(12))
        },
        label = { Text("MAC-адрес") },
        placeholder = {
            Text(
                "XX-XX-XX-XX-XX-XX",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onPaste) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Вставить из буфера"
                )
            }
        },
        visualTransformation = visualTransformation,
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
 * VisualTransformation that inserts dashes between octets for display only.
 * The actual value stays as raw hex (e.g. "0015F2204D6B"),
 * but the display shows "00-15-F2-20-4D-6B".
 *
 * OffsetMapping translates cursor positions between raw and formatted text.
 */
private class MacVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text.text.chunked(2).joinToString("-")

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Each pair of hex chars adds a dash after it (except the last)
                // Raw:  0 1 2 3 4 5 6 7 8 9 10 11
                // Disp: 0 1 - 3 4 - 6 7 - 9 10 - 12 13
                if (offset <= 1) return offset
                val groupIndex = (offset - 1) / 2  // which group pair we're in
                val posInGroup = (offset - 1) % 2   // position within that pair
                return offset + groupIndex + (if (posInGroup > 0) 1 else 0)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Reverse mapping: strip dashes to find raw position
                if (offset <= 1) return offset
                // Formatted positions: every 3rd char is a dash (at 2, 5, 8, 11, 14)
                // Find how many dashes are before this position
                val dashCount = (offset - 1) / 3
                val result = offset - dashCount
                return result.coerceAtMost(text.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
