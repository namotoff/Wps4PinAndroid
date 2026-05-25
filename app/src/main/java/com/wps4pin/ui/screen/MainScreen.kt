// Copyright (c) 2024-2026 Bios Tlt. Licensed under Apache License 2.0.
//

package com.wps4pin.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.wps4pin.ads.AdSlot
import com.wps4pin.data.HistoryEntry
import com.wps4pin.data.HistoryRepository
import com.wps4pin.logic.PinCalculator
import com.wps4pin.ui.component.DisclaimerDialog
import com.wps4pin.ui.component.MacInputField
import com.wps4pin.ui.component.PinResultCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val APP_VERSION = "1.0.1"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    historyRepository: HistoryRepository,
    modifier: Modifier = Modifier
) {
    var macInput by remember { mutableStateOf("") }
    val pinResult = remember(macInput) { PinCalculator.calculate(macInput) }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val historyEntries by historyRepository.history.collectAsState(initial = emptyList())
    val disclaimerAccepted by historyRepository.disclaimerAccepted.collectAsState(initial = null)
    var disclaimerLoaded by remember { mutableStateOf(false) }
    val showDisclaimer = disclaimerLoaded && disclaimerAccepted == false

    var showMenu by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(disclaimerAccepted) {
        if (disclaimerAccepted != null) {
            disclaimerLoaded = true
        }
    }

    LaunchedEffect(pinResult) {
        if (pinResult != null && macInput.isNotBlank()) {
            historyRepository.add(macInput.trim().uppercase(), pinResult)
        }
    }

    if (showDisclaimer) {
        DisclaimerDialog(
            onAccept = {
                disclaimerLoaded = false
                scope.launch {
                    historyRepository.setDisclaimerAccepted()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WPS4PIN") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Меню",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Расчёт WPS PIN по MAC",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(20.dp))
            }

            item {
                MacInputField(
                    value = macInput,
                    onValueChange = { macInput = it },
                    onPaste = {
                        val clipText = clipboardManager.getText()?.text
                        if (!clipText.isNullOrBlank()) {
                            val cleaned = clipText.trim().uppercase()
                                .filter { c -> c in '0'..'9' || c in 'A'..'F' }
                            macInput = cleaned.take(12)
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                PinResultCard(
                    pin = pinResult,
                    onCopy = {
                        if (pinResult != null) {
                            clipboardManager.setText(AnnotatedString(pinResult))
                        }
                    }
                )
                Spacer(Modifier.height(24.dp))
            }

            if (historyEntries.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "История",
                                style = MaterialTheme.typography.titleLarge
                            )
                            TextButton(onClick = {
                                scope.launch {
                                    historyRepository.clear()
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Очистить")
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                items(historyEntries, key = { it.timestamp }) { entry ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                scope.launch { historyRepository.remove(entry.timestamp) }
                            }
                            it == SwipeToDismissBoxValue.EndToStart
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                                    .padding(end = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Удалить",
                                    tint = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Card(
                            onClick = { macInput = entry.mac },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.mac,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.pin,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatTimestamp(entry.timestamp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(entry.pin)) }) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Копировать",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                AdSlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // --- Menu BottomSheet ---
    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Меню",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                MenuItem(
                    icon = Icons.Outlined.Info,
                    label = "О приложении",
                    onClick = {
                        showMenu = false
                        showAbout = true
                    }
                )

                MenuItem(
                    icon = Icons.Outlined.Shield,
                    label = "Политика конфиденциальности",
                    onClick = {
                        showMenu = false
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://namotoff.github.io/wps4pin-privacy/")))
                    }
                )

                MenuItem(
                    icon = Icons.Outlined.Mail,
                    label = "Написать разработчику",
                    onClick = {
                        showMenu = false
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:edazin@bk.ru"))
                                .apply { putExtra(Intent.EXTRA_SUBJECT, "WPS4PIN") }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // --- About dialog ---
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("WPS4PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Версия $APP_VERSION", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text("Инструмент расчёта WPS PIN-кода по MAC-адресу сетевого устройства. Предназначен только для аудита собственных сетей.", fontSize = 14.sp)
                    Text("Разработчик: Bios Tlt", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}
