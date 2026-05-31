package com.smartboard.ime.layouts

object AvroStyleComposer {
    private val map = mapOf(
        "a" to "আ",
        "aa" to "আ",
        "e" to "এ",
        "i" to "ই",
        "o" to "ও",
        "u" to "উ",
        "k" to "ক",
        "kh" to "খ",
        "g" to "গ",
        "gh" to "ঘ",
        "d" to "দ",
        "t" to "ত",
        "n" to "ন",
        "p" to "প",
        "b" to "ব",
        "m" to "ম",
        "r" to "র",
        "l" to "ল",
    )

    fun commitKey(currentBuffer: String, key: String): Pair<String, String> {
        if (key.isNotEmpty() && key[0].code > 127 && key != "en") {
            return "" to key
        }
        val lower = key.lowercase()
        val buf = currentBuffer + lower
        map.entries.firstOrNull { buf.endsWith(it.key) }?.let {
            val out = it.value
            val keep = buf.dropLast(it.key.length)
            return keep to out
        }
        if (lower.length == 1) {
            map[lower]?.let { return "" to it }
        }
        return buf to ""
    }
}
