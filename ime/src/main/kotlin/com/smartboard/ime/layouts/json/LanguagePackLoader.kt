package com.smartboard.ime.layouts.json

import android.content.Context
import com.smartboard.ime.layouts.KeyboardLayout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagePackLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val dtoCache = ConcurrentHashMap<String, LayoutJsonRoot>()
    private val builtCache = ConcurrentHashMap<String, KeyboardLayout>()
    private val mutex = Mutex()

    suspend fun loadDto(locale: String): LayoutJsonRoot = withContext(Dispatchers.IO) {
        dtoCache.getOrPut(locale) {
            context.assets.open("layouts/$locale.json").use { stream ->
                json.decodeFromStream(LayoutJsonRoot.serializer(), stream)
            }
        }
    }

    suspend fun loadKeyboard(
        locale: String,
        symbolsMode: Boolean,
        includeGlobeKey: Boolean,
    ): KeyboardLayout = withContext(Dispatchers.IO) {
        val cacheKey = "$locale|${if (symbolsMode) "sym" else "abc"}|$includeGlobeKey"
        builtCache[cacheKey] ?: mutex.withLock {
            builtCache.getOrPut(cacheKey) {
                val dto = loadDto(locale)
                KeyboardLayoutAssembler.build(dto, symbolsMode, includeGlobeKey)
            }
        }
    }

    suspend fun loadAllMeta(): List<LanguageMeta> = withContext(Dispatchers.IO) {
        val names = context.assets.list(LAYOUT_ASSET_DIR) ?: return@withContext emptyList()
        names.filter { it.endsWith(".json") }
            .mapNotNull { file ->
                val loc = file.removeSuffix(".json")
                runCatching { loadDto(loc).toMeta() }.getOrNull()
            }
            .sortedBy { it.displayName }
    }

    companion object {
        const val LAYOUT_ASSET_DIR = "layouts"
    }
}
