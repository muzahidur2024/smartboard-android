package com.smartboard.ime.layouts

enum class KeyWidth { NARROW, NORMAL, WIDE, SPACE, FULL }

enum class KeyAction {
    CHARACTER,
    BACKSPACE,
    SHIFT,
    ENTER,
    SPACE,
    SWITCH_LANGUAGE,
    EMOJI,
    CLIPBOARD,
    NEWLINE,
    TAB,
    GLOBE,
    VOICE,
    GIF,
    SYMBOLS,
}

data class KeyDef(
    val primary: String,
    val primaryShift: String? = null,
    val secondary: String? = null,
    val tertiary: String? = null,
    val width: KeyWidth = KeyWidth.NORMAL,
    val action: KeyAction = KeyAction.CHARACTER,
)

data class KeyRow(val keys: List<KeyDef>)

data class KeyboardLayout(
    val name: String,
    val locale: String,
    val nativeDisplayName: String = name,
    val direction: String = "ltr",
    val family: String = "latin",
    val hasShiftVariants: Boolean = true,
    val hasNumberRowInPack: Boolean = true,
    val rows: List<KeyRow>,
    val numberRow: List<KeyDef>? = null,
    val symbolRows: List<KeyRow>? = null,
)
