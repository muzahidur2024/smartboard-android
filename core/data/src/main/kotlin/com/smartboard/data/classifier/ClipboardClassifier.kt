package com.smartboard.data.classifier

import com.smartboard.model.ClipboardCategory
import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClipboardClassifier @Inject constructor() {

    private val urlRegex = Patterns.WEB_URL.toRegex()
    private val emailRegex = Patterns.EMAIL_ADDRESS.toRegex()
    private val phoneRegex = Patterns.PHONE.toRegex()

    fun classify(text: String): ClipboardCategory {
        val trimmed = text.trim()
        return when {
            urlRegex.containsMatchIn(trimmed) -> ClipboardCategory.LINK
            emailRegex.matches(trimmed) -> ClipboardCategory.EMAIL
            phoneRegex.matches(trimmed) && trimmed.length < 20 -> ClipboardCategory.PHONE
            else -> ClipboardCategory.PLAIN
        }
    }

    suspend fun classifyAsync(text: String): ClipboardCategory =
        withContext(Dispatchers.Default) { classify(text) }
}
