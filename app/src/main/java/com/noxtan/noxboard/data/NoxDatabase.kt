package com.noxtan.noxboard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SuggestionEntity::class], version = 1, exportSchema = false)
abstract class NoxDatabase : RoomDatabase() {
    abstract fun suggestionDao(): SuggestionDao

    companion object {
        @Volatile
        private var INSTANCE: NoxDatabase? = null

        fun getDatabase(context: Context): NoxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoxDatabase::class.java,
                    "nox_suggestions.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}