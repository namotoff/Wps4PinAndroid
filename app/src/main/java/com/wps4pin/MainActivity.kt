// Copyright (c) 2024-2026 Bios Tlt. Licensed under Apache License 2.0.
//

package com.wps4pin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wps4pin.data.HistoryRepository
import com.wps4pin.ui.screen.MainScreen
import com.wps4pin.ui.theme.Wps4pinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val historyRepository = HistoryRepository(this)

        setContent {
            Wps4pinTheme {
                MainScreen(historyRepository = historyRepository)
            }
        }
    }
}
