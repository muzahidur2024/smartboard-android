package com.smartboard.ime.layouts.json

import com.smartboard.ime.layouts.KeyAction
import com.smartboard.ime.layouts.KeyDef
import com.smartboard.ime.layouts.KeyRow
import com.smartboard.ime.layouts.KeyWidth
import com.smartboard.ime.layouts.KeyboardLayout

object KeyboardLayoutAssembler {

    fun build(
        dto: LayoutJsonRoot,
        symbolsMode: Boolean,
        includeGlobeKey: Boolean,
    ): KeyboardLayout {
        val numberRow = dto.specialRows?.numberRow?.keys?.map { ch ->
            KeyDef(
                primary = ch,
                primaryShift = null,
                secondary = null,
                width = KeyWidth.NORMAL,
                action = KeyAction.CHARACTER,
            )
        }.takeIf { !it.isNullOrEmpty() }

        val symbolRows = buildSymbolRows(dto)

        val letterRows = when {
            symbolsMode && symbolRows != null ->
                buildSymbolLayoutRows(dto, symbolRows, includeGlobeKey)
            symbolsMode -> buildLetterRows(dto, includeGlobeKey)
            dto.rows.isEmpty() -> {
                val fk = dto.funcKeys
                if (fk != null) {
                    listOf(buildBottomRow(dto, fk, includeGlobeKey))
                } else {
                    emptyList()
                }
            }
            else -> buildLetterRows(dto, includeGlobeKey)
        }

        return KeyboardLayout(
            name = dto.displayName,
            locale = dto.locale,
            nativeDisplayName = dto.nativeDisplayName,
            direction = dto.direction,
            family = dto.family,
            hasShiftVariants = dto.hasShiftVariants,
            hasNumberRowInPack = dto.hasNumberRow,
            numberRow = numberRow,
            symbolRows = symbolRows,
            rows = letterRows,
        )
    }

    private fun buildSymbolRows(dto: LayoutJsonRoot): List<KeyRow>? {
        val page = dto.specialRows?.symbolPage1 ?: return null
        return when {
            !page.rows.isNullOrEmpty() -> page.rows.map { row ->
                KeyRow(row.keys.map { it.toCharKeyDef() })
            }
            !page.keys.isNullOrEmpty() -> listOf(KeyRow(page.keys.map { it.toCharKeyDef() }))
            else -> null
        }
    }

    /**
     * Symbol pages in the language packs only contain the symbol keys. Without a functional bottom
     * row the user would be stranded with no space / enter / backspace and no way back to letters,
     * so we append a backspace to the last symbol row and a bottom row whose "?123" key is relabeled
     * "ABC" to return to the alphabetic layout.
     */
    private fun buildSymbolLayoutRows(
        dto: LayoutJsonRoot,
        symbolRows: List<KeyRow>,
        includeGlobeKey: Boolean,
    ): List<KeyRow> {
        val fk = dto.funcKeys ?: return symbolRows
        val rows = symbolRows.toMutableList()
        fk.delete?.let { del ->
            if (rows.isNotEmpty()) {
                val last = rows.removeAt(rows.lastIndex)
                rows.add(KeyRow(last.keys + del.toFuncKeyDef(KeyAction.BACKSPACE, "⌫")))
            }
        }
        rows.add(buildBottomRow(dto, fk, includeGlobeKey, symbolsMode = true))
        return rows
    }

    private fun buildLetterRows(dto: LayoutJsonRoot, includeGlobeKey: Boolean): List<KeyRow> {
        val fk = dto.funcKeys ?: return dto.rows.map { KeyRow(it.keys.map { k -> k.toCharKeyDef() }) }

        val body = dto.rows.mapIndexed { index, row ->
            if (dto.rows.isNotEmpty() && index == dto.rows.lastIndex) {
                val keys = mutableListOf<KeyDef>()
                fk.shift?.let { keys.add(it.toFuncKeyDef(KeyAction.SHIFT, "⇧")) }
                keys.addAll(row.keys.map { it.toCharKeyDef() })
                fk.delete?.let { keys.add(it.toFuncKeyDef(KeyAction.BACKSPACE, "⌫")) }
                KeyRow(keys)
            } else {
                KeyRow(row.keys.map { it.toCharKeyDef() })
            }
        }.toMutableList()

        body.add(buildBottomRow(dto, fk, includeGlobeKey))
        return body
    }

    private fun buildBottomRow(
        dto: LayoutJsonRoot,
        fk: FuncKeysJson,
        includeGlobeKey: Boolean,
        symbolsMode: Boolean = false,
    ): KeyRow {
        val keys = mutableListOf<KeyDef>()
        // The symbols key doubles as the "back to letters" key, so force its label.
        fk.symbols?.let { sk ->
            keys.add(
                KeyDef(
                    primary = if (symbolsMode) "ABC" else (sk.label ?: "?123"),
                    width = sk.width?.toKeyWidth() ?: defaultWidth(KeyAction.SYMBOLS),
                    action = KeyAction.SYMBOLS,
                ),
            )
        }
        if (includeGlobeKey) {
            fk.languageSwitch?.let {
                keys.add(it.toFuncKeyDef(KeyAction.SWITCH_LANGUAGE, it.label ?: "🌐"))
            }
        }
        fk.emoji?.let { keys.add(it.toFuncKeyDef(KeyAction.EMOJI, it.label ?: "😀")) }
        fk.space?.let { keys.add(it.toFuncKeyDef(KeyAction.SPACE, it.label ?: "")) }
        fk.clipboard?.let { keys.add(it.toFuncKeyDef(KeyAction.CLIPBOARD, it.label ?: "📋")) }
        fk.enter?.let { keys.add(it.toFuncKeyDef(KeyAction.ENTER, it.label ?: "↵")) }
        return KeyRow(keys)
    }

    private fun LayoutKeyJson.toCharKeyDef(): KeyDef = KeyDef(
        primary = primary,
        primaryShift = primaryShift ?: primary.uppercase(),
        secondary = secondary,
        tertiary = alternates.joinToString("").takeIf { it.isNotEmpty() },
        width = width.toKeyWidth(),
        action = KeyAction.CHARACTER,
    )

    private fun FuncKeyJson.toFuncKeyDef(fallbackAction: KeyAction, defaultLabel: String): KeyDef {
        val resolved = parseAction(this.action) ?: fallbackAction
        val lbl = this.label ?: defaultLabel
        return KeyDef(
            primary = lbl,
            primaryShift = null,
            width = this.width?.toKeyWidth() ?: defaultWidth(resolved),
            action = resolved,
        )
    }

    private fun defaultWidth(action: KeyAction): KeyWidth = when (action) {
        KeyAction.SPACE -> KeyWidth.SPACE
        KeyAction.ENTER, KeyAction.BACKSPACE, KeyAction.SHIFT -> KeyWidth.WIDE
        KeyAction.SWITCH_LANGUAGE, KeyAction.SYMBOLS, KeyAction.EMOJI, KeyAction.CLIPBOARD -> KeyWidth.NARROW
        else -> KeyWidth.NORMAL
    }

    private fun parseAction(raw: String): KeyAction? = when (raw.uppercase()) {
        "SHIFT" -> KeyAction.SHIFT
        "BACKSPACE" -> KeyAction.BACKSPACE
        "ENTER" -> KeyAction.ENTER
        "SPACE" -> KeyAction.SPACE
        "SWITCH_LANGUAGE", "LANGUAGE" -> KeyAction.SWITCH_LANGUAGE
        "EMOJI" -> KeyAction.EMOJI
        "CLIPBOARD" -> KeyAction.CLIPBOARD
        "SYMBOLS" -> KeyAction.SYMBOLS
        else -> null
    }

    private fun String.toKeyWidth(): KeyWidth = when (lowercase()) {
        "narrow" -> KeyWidth.NARROW
        "wide" -> KeyWidth.WIDE
        "space" -> KeyWidth.SPACE
        "full" -> KeyWidth.FULL
        else -> KeyWidth.NORMAL
    }
}

fun LayoutJsonRoot.toMeta(): LanguageMeta = LanguageMeta(
    locale = locale,
    displayName = displayName,
    nativeDisplayName = nativeDisplayName,
    direction = direction,
    flagEmoji = flagEmoji,
)

data class LanguageMeta(
    val locale: String,
    val displayName: String,
    val nativeDisplayName: String,
    val direction: String,
    val flagEmoji: String,
)
