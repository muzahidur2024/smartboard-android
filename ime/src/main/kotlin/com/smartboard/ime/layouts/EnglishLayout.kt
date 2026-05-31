package com.smartboard.ime.layouts

import com.smartboard.ime.layouts.KeyAction.CHARACTER

/** Fallback digit row when a language pack omits [KeyboardLayout.numberRow]. */
fun englishNumbersRow(): List<KeyDef> = listOf(
    KeyDef("1", secondary = "!"),
    KeyDef("2", secondary = "@"),
    KeyDef("3", secondary = "#"),
    KeyDef("4", secondary = "$"),
    KeyDef("5", secondary = "%"),
    KeyDef("6", secondary = "^"),
    KeyDef("7", secondary = "&"),
    KeyDef("8", secondary = "*"),
    KeyDef("9", secondary = "("),
    KeyDef("0", secondary = ")"),
)

fun placeholderKeyboardLayout(): KeyboardLayout = KeyboardLayout(
    name = "…",
    locale = "en_US",
    nativeDisplayName = "…",
    rows = listOf(KeyRow(listOf(KeyDef(" ", action = CHARACTER)))),
)
