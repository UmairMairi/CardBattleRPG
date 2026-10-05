package com.pegasus.cardbattlerpg.utils

// Basic word filter for user generated text (chat, guild names, player names).
// Play's UGC policy requires safeguards against objectionable content; this is the
// first line of defence, backed by in-app reporting/blocking and manual moderation.
object ContentFilter {
    const val MAX_CHAT_LENGTH = 200
    const val MAX_NAME_LENGTH = 20
    const val MAX_DESCRIPTION_LENGTH = 120

    // English + Indonesian profanity/slurs. Extend this list as reports come in.
    private val blockedWords = listOf(
        "fuck", "shit", "bitch", "bastard", "asshole", "dick", "pussy", "cunt", "whore", "slut",
        "nigger", "nigga", "faggot", "retard", "rape", "porn", "sex", "nude",
        "anjing", "anjg", "bangsat", "bajingan", "kontol", "memek", "ngentot", "entot", "jancuk",
        "jancok", "goblok", "tolol", "babi", "brengsek", "keparat", "pelacur", "lonte", "perek",
        "kampret", "tai", "asu", "pantek", "pukimak", "ngewe", "bokep", "coli"
    )

    private fun normalize(text: String): String = text.lowercase()
        .replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a')
        .replace('5', 's').replace('7', 't').replace('@', 'a').replace('$', 's')

    private fun regexFor(word: String) = Regex("(?<![a-z])${Regex.escape(word)}(?![a-z])")

    fun containsBlockedWord(text: String): Boolean {
        val normalized = normalize(text)
        return blockedWords.any { regexFor(it).containsMatchIn(normalized) }
    }

    // Replaces blocked words with asterisks, keeping the original casing of other text.
    fun mask(text: String): String {
        val normalized = normalize(text)
        val chars = text.toCharArray()
        blockedWords.forEach { word ->
            regexFor(word).findAll(normalized).forEach { match ->
                for (i in match.range) if (i < chars.size) chars[i] = '*'
            }
        }
        return String(chars)
    }

    // Returns an error message when a name/title is not allowed, or null when it is fine.
    fun validateName(name: String, label: String = "Nama"): String? {
        val clean = name.trim()
        return when {
            clean.isBlank() -> "$label tidak boleh kosong"
            clean.length > MAX_NAME_LENGTH -> "$label maksimal $MAX_NAME_LENGTH karakter"
            containsBlockedWord(clean) -> "$label mengandung kata yang tidak pantas"
            else -> null
        }
    }
}
