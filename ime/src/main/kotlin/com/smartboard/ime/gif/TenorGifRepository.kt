package com.smartboard.ime.gif

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tenor integration (Phase 3). Provide API key via BuildConfig in the app module.
 */
@Singleton
class TenorGifRepository @Inject constructor() {
    fun trending(query: String, apiKey: String): List<String> {
        if (apiKey.isBlank()) return emptyList()
        // Stub: network call omitted; keep interface for later wiring.
        return emptyList()
    }
}
