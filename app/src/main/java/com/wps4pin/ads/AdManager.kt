package com.wps4pin.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Ad slot interface — placeholder for future AdMob / Yandex Ads integration.
 * Replacing the stub with a real implementation should not require UI refactoring.
 */
interface AdManager {
    /** Load and prepare banner ad. No-op in stub. */
    fun loadBanner() {}

    /** Show banner ad. No-op in stub. */
    fun showBanner() {}

    /** Destroy / release ad resources. No-op in stub. */
    fun destroy() {}
}

/** Stub implementation — does nothing, reserves the ad slot in UI. */
object StubAdManager : AdManager

/**
 * Composable ad slot placeholder. Renders an empty reserved space
 * at the bottom of the screen. When a real AdManager is injected,
 * replace the inner content with the actual ad view.
 */
@Composable
fun AdSlot(
    modifier: Modifier = Modifier,
    adManager: AdManager = StubAdManager
) {
    // Reserved space — currently invisible. Replace with AdView when SDK is integrated.
    // Example future usage: AndroidView(factory = { adManager.createBannerView(it) })
}
