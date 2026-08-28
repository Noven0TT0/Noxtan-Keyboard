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

    fun searchFuzzy(targetWord: String, maxDist: Int = 1, limit: Int = 5): List<String> {
        if (targetWord.isBlank()) return emptyList()
        val results = mutableListOf<Pair<String, Int>>()
        val currentRow = IntArray(targetWord.length + 1) { it }

        for ((char, childNode) in root.children) {
            searchRecursive(childNode, char, targetWord, currentRow, results, maxDist)
        }

        return results.sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    private fun searchRecursive(
        node: TrieNode,
        char: Char,
        targetWord: String,
        previousRow: IntArray,
        results: MutableList<Pair<String, Int>>,
        maxDist: Int
    ) {
        val columns = targetWord.length + 1
        val currentRow = IntArray(columns)
        currentRow[0] = previousRow[0] + 1

        var minDistance = currentRow[0]

        for (c in 1 until columns) {
            val insertCost = currentRow[c - 1] + 1
            val deleteCost = previousRow[c] + 1
            val replaceCost = if (targetWord[c - 1] == char) previousRow[c - 1] else previousRow[c - 1] + 1

            currentRow[c] = minOf(insertCost, deleteCost, replaceCost)
            if (currentRow[c] < minDistance) {
                minDistance = currentRow[c]
            }
        }

        if (currentRow[targetWord.length] <= maxDist && node.isEndOfWord) {
            results.add(Pair(node.savedWord, node.frequency))
        }

        if (minDistance <= maxDist) {
            for ((childChar, childNode) in node.children) {
                searchRecursive(childNode, childChar, targetWord, currentRow, results, maxDist)
            }
        }
    }
}