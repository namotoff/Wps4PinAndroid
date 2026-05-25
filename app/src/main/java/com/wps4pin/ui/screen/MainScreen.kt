package com.wps4pin.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.wps4pin.ads.AdSlot
import com.wps4pin.data.HistoryEntry
import com.wps4pin.data.HistoryRepository
import com.wps4pin.logic.PinCalculator
import com.wps4pin.ui.component.DisclaimerDialog
import com.wps4pin.ui.component.MacInputField
import com.wps4pin.ui.component.PinResultCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val historyEntries by historyRepository.history.collectAsState(initial = emptyList())
    val disclaimerAccepted by historyRepository.disclaimerAccepted.collectAsState(initial = null)

    var showDisclaimer by remember { mutableStateOf(false) }
    var disclaimerLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(disclaimerAccepted) {
        if (disclaimerAccepted == null) return@LaunchedEffect // still loading
        disclaimerLoaded = true
        if (disclaimerAccepted == false) {
            showDisclaimer = true
        } else {
            showDisclaimer = false
        }
    }

    LaunchedEffect(pinResult) {
        if (pinResult != null && macInput.isNotBlank()) {
            historyRepository.add(macInput.trim().uppercase(), pinResult)
        }
    }

    if (disclaimerLoaded && showDisclaimer && disclaimerAccepted != true) {
        DisclaimerDialog(
            onAccept = {
                showDisclaimer = false
            }
        )
    }

    LaunchedEffect(showDisclaimer) {
        if (!showDisclaimer && disclaimerAccepted != true) {
            historyRepository.setDisclaimerAccepted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WPS4PIN") },
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
                            macInput = clipText.trim()
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
                            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceVariant

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
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
}

private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}
