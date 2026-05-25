// Copyright (c) 2024-2026 Bios Tlt. Licensed under Apache License 2.0.
//

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
 * - Raw value is pure hex (e.g. "0015F2204D6B"), dashes shown only visually
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
        visualTransformation = MacVisualTransformation,
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
 * Builds explicit position map tables — no formula bugs.
 *
 * Raw:  "0015F2"     →  Display: "00-15-F2"
 * Index: 012345          Index:   012345678
 */
private object MacVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = raw.chunked(2).joinToString("-")

        // Build explicit bidirectional position maps
        // o2t[i] = formatted cursor position for raw cursor position i
        // t2o[j] = raw cursor position for formatted cursor position j
        val o2t = mutableListOf(0)
        val t2o = mutableListOf(0)

        var ri = 0  // raw index
        var fi = 0  // formatted index
        while (ri < raw.length) {
            val chunk = minOf(2, raw.length - ri)
            for (i in 0 until chunk) {
                ri++
                fi++
                o2t.add(fi)
                t2o.add(ri)
            }
            // Insert dash between pairs (not after the last one)
            if (ri < raw.length) {
                fi++  // dash occupies one formatted position
                t2o.add(ri)  // cursor on dash → same raw position as start of next pair
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset < 0) return 0
                if (offset >= o2t.size) return formatted.length
                return o2t[offset]
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset < 0) return 0
                if (offset >= t2o.size) return raw.length
                return t2o[offset]
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
