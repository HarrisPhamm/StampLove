package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Category
import com.example.data.model.Stamp
import com.example.data.model.Album

@Database(entities = [Stamp::class, Category::class, Album::class], version = 2, exportSchema = false)
abstract class StampDatabase : RoomDatabase() {
    abstract fun stampDao(): StampDao
    abstract fun categoryDao(): CategoryDao
    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile
        private var INSTANCE: StampDatabase? = null

        fun getDatabase(context: Context): StampDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StampDatabase::class.java,
                    "stampify_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
