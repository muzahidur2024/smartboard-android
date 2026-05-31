package com.smartboard.ime.layouts.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LayoutJsonRoot(
    val locale: String,
    val displayName: String,
    val nativeDisplayName: String,
    val direction: String = "ltr",
    val family: String = "latin",
    val hasShiftVariants: Boolean = true,
    val hasNumberRow: Boolean = true,
    val flagEmoji: String = "",
    val rows: List<LayoutRowJson> = emptyList(),
    val specialRows: SpecialRowsJson? = null,
    val funcKeys: FuncKeysJson? = null,
)

@Serializable
data class LayoutRowJson(
    val keys: List<LayoutKeyJson> = emptyList(),
)

@Serializable
data class LayoutKeyJson(
    val primary: String,
    @SerialName("primaryShift")
    val primaryShift: String? = null,
    val secondary: String? = null,
    val alternates: List<String> = emptyList(),
    val width: String = "normal",
)

@Serializable
data class SpecialRowsJson(
    val numberRow: NumberRowJson? = null,
    val symbolPage1: SymbolPageJson? = null,
)

@Serializable
data class NumberRowJson(
    val keys: List<String> = emptyList(),
)

@Serializable
data class SymbolPageJson(
    val keys: List<LayoutKeyJson>? = null,
    val rows: List<LayoutRowJson>? = null,
)

@Serializable
data class FuncKeysJson(
    val shift: FuncKeyJson? = null,
    val delete: FuncKeyJson? = null,
    val enter: FuncKeyJson? = null,
    val space: FuncKeyJson? = null,
    val languageSwitch: FuncKeyJson? = null,
    val symbols: FuncKeyJson? = null,
    val emoji: FuncKeyJson? = null,
    val clipboard: FuncKeyJson? = null,
)

@Serializable
data class FuncKeyJson(
    val action: String,
    val label: String? = null,
    val width: String? = null,
)
