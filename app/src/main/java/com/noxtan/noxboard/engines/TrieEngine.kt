package com.noxtan.noxboard.engines

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isEndOfWord: Boolean = false
    var frequency: Int = 0
    var savedWord: String = ""
}

class TrieEngine {
    private val root = TrieNode()

    fun insert(word: String, frequency: Int = 1) {
        if (word.isBlank()) return
        var curr = root
        for (char in word.lowercase()) {
            if (!curr.children.containsKey(char)) {
                curr.children[char] = TrieNode()
            }
            curr = curr.children[char]!!
        }
        curr.isEndOfWord = true
        curr.frequency += frequency

        if (curr.savedWord.isEmpty() || word.any { it.isUpperCase() }) {
            curr.savedWord = word
        }
    }

    fun searchPrefix(prefix: String, limit: Int = 10): List<String> {
        if (prefix.isBlank()) return emptyList()

        var curr = root
        for (char in prefix.lowercase()) {
            curr = curr.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Int>>()
        findWordsFromNode(curr, results)

        return results.sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    private fun findWordsFromNode(node: TrieNode, results: MutableList<Pair<String, Int>>) {
        if (node.isEndOfWord) {
            results.add(Pair(node.savedWord, node.frequency))
        }
        for ((_, childNode) in node.children) {
            findWordsFromNode(childNode, results)
        }
    }

    fun delete(word: String) {
        if (word.isBlank()) return
        var curr = root
        for (char in word.lowercase()) {
            curr = curr.children[char] ?: return
        }
        if (curr.isEndOfWord) {
            curr.isEndOfWord = false
            curr.frequency = 0
            curr.savedWord = ""
        }
    }

    fun clear() {
        root.children.clear()
    }
}