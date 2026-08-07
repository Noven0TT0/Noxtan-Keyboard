package com.noxtan.noxboard.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "suggestions", indices = [Index(value = ["word", "nextWord"], unique = true)])
data class SuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val nextWord: String,
    val frequency: Int = 1
)