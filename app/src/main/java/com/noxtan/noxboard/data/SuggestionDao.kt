package com.noxtan.noxboard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SuggestionDao {
    @Query("SELECT word FROM suggestions WHERE word LIKE :prefix || '%' GROUP BY word ORDER BY SUM(frequency) DESC LIMIT 10")
    suspend fun getWordCompletions(prefix: String): List<String>

    @Query("SELECT nextWord FROM suggestions WHERE word = :word ORDER BY frequency DESC LIMIT 10")
    suspend fun getNextWords(word: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(suggestion: SuggestionEntity): Long

    @Query("UPDATE suggestions SET frequency = frequency + :amount WHERE word = :word AND nextWord = :nextWord")
    suspend fun incrementFrequency(word: String, nextWord: String, amount: Int)

    @Transaction
    suspend fun upsert(word: String, nextWord: String, amount: Int = 1) {
        val id = insert(SuggestionEntity(word = word, nextWord = nextWord, frequency = amount))
        if (id == -1L) {
            incrementFrequency(word, nextWord, amount)
        }
    }

    @Query("DELETE FROM suggestions")
    suspend fun deleteAllSuggestions()

    @Query("DELETE FROM suggestions WHERE word = :word OR nextWord = :word")
    suspend fun deleteWord(word: String)

    @Query("DELETE FROM suggestions WHERE id NOT IN (SELECT id FROM suggestions ORDER BY frequency DESC LIMIT 5000)")
    suspend fun cleanupOldWords()

    @Query("SELECT frequency FROM suggestions WHERE word = :word LIMIT 1")
    suspend fun getWordFrequency(word: String): Int?

    @Query("SELECT * FROM suggestions")
    suspend fun getAllSuggestions(): List<SuggestionEntity>
}