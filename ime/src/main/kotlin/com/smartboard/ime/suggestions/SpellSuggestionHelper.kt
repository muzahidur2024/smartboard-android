package com.smartboard.ime.suggestions

import android.content.Context
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener
import android.view.textservice.SuggestionsInfo
import android.view.textservice.TextInfo
import android.view.textservice.TextServicesManager
import java.util.Locale

/**
 * Bridges Android spell-checker sessions to candidate words for the suggestion strip.
 */
class SpellSuggestionHelper(
    context: Context,
    private val locale: Locale,
    private val onSuggestions: (List<String>) -> Unit,
) : SpellCheckerSessionListener {
    private val tsm = context.getSystemService(TextServicesManager::class.java)
    private var session: SpellCheckerSession? = null

    fun start() {
        session = tsm?.newSpellCheckerSession(null, locale, this, true)
    }

    fun request(word: String) {
        session?.getSentenceSuggestions(arrayOf(TextInfo(word)), 3)
    }

    fun close() {
        session?.close()
        session = null
    }

    override fun onGetSuggestions(results: Array<out SuggestionsInfo>?) {
        val words = results?.flatMap { si ->
            (0 until si.suggestionsCount).mapNotNull { si.getSuggestionAt(it) }
        }.orEmpty()
        onSuggestions(words.distinct())
    }

    override fun onGetSentenceSuggestions(results: Array<out SentenceSuggestionsInfo>?) {
        val words = results?.flatMap { ssi ->
            (0 until ssi.suggestionsCount).flatMap { i ->
                val si = ssi.getSuggestionsInfoAt(i)
                (0 until si.suggestionsCount).mapNotNull { si.getSuggestionAt(it) }
            }
        }.orEmpty()
        onSuggestions(words.distinct())
    }
}
