package com.noxtan.noxboard.engines

class NextWordEngine {
    private val bigrams = HashMap<String, HashMap<String, Int>>()

    fun insert(word: String, nextWord: String, frequency: Int = 1) {
        if (word.isBlank() || nextWord.isBlank()) return
        val nextWordsMap = bigrams.getOrPut(word) { HashMap() }
        nextWordsMap[nextWord] = (nextWordsMap[nextWord] ?: 0) + frequency
    }

    fun findTriggerKey(text: String, chosenWord: String): String? {
        if (bigrams.containsKey(text) && bigrams[text]?.containsKey(chosenWord) == true) {
            return text
        }
        for ((key, nextWordsMap) in bigrams) {
            if (text.endsWith(key) && text != key) {
                if (nextWordsMap.containsKey(chosenWord)) return key
            } else {
                val index = text.lastIndexOf(key)
                if (index != -1 && index + key.length < text.length) {
                    val remainder = text.substring(index + key.length)
                    if (chosenWord.startsWith(remainder) && nextWordsMap.containsKey(chosenWord)) {
                        return key
                    }
                }
            }
        }
        return null
    }

    fun predictContinuous(text: String, limit: Int = 5): List<String> {
        val results = mutableMapOf<String, Int>()

        if (bigrams.containsKey(text)) {
            bigrams[text]?.forEach { (nextWord, freq) ->
                results[nextWord] = (results[nextWord] ?: 0) + freq
            }
        }

        for ((key, nextWordsMap) in bigrams) {
            if (text.endsWith(key) && text != key) {
                for ((nextWord, freq) in nextWordsMap) {
                    results[nextWord] = (results[nextWord] ?: 0) + freq
                }
            } else {
                val index = text.lastIndexOf(key)
                if (index != -1 && index + key.length < text.length) {
                    val remainder = text.substring(index + key.length)
                    for ((nextWord, freq) in nextWordsMap) {
                        if (nextWord.startsWith(remainder) && nextWord != remainder) {
                            results[nextWord] = (results[nextWord] ?: 0) + freq
                        }
                    }
                }
            }
        }

        return results.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    fun getFrequency(word: String, nextWord: String): Int {
        return bigrams[word]?.get(nextWord) ?: 0
    }

    fun clear() {
        bigrams.clear()
    }

    fun getExactNextWords(word: String, limit: Int = 5): List<String> {
        val exactMatch = bigrams[word]
        val lowerMatch = bigrams[word.lowercase()]
        val upperMatch = bigrams[word.replaceFirstChar { it.uppercase() }]

        val combinedMap = mutableMapOf<String, Int>()
        exactMatch?.forEach { (k, v) -> combinedMap[k] = (combinedMap[k] ?: 0) + v }
        lowerMatch?.forEach { (k, v) -> combinedMap[k] = (combinedMap[k] ?: 0) + v }
        upperMatch?.forEach { (k, v) -> combinedMap[k] = (combinedMap[k] ?: 0) + v }

        return combinedMap.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    fun getMissingSpaceWords(text: String, limit: Int = 5): List<String> {
        val results = mutableListOf<Pair<String, Int>>()
        val lowerText = text.lowercase()
        for ((key, nextWordsMap) in bigrams) {
            if (lowerText.startsWith(key.lowercase()) && lowerText.length > key.length) {
                val remainder = lowerText.substring(key.length)
                for ((nextWord, freq) in nextWordsMap) {
                    if (nextWord.lowercase().startsWith(remainder)) {
                        results.add(Pair("$key $nextWord", freq))
                    }
                }
            }
        }
        return results.sortedByDescending { it.second }.take(limit).map { it.first }
    }
}