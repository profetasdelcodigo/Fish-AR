package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CaughtFishEntity::class, UserSettingEntity::class, FairLeaderboardEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MarineDatabase : RoomDatabase() {
    abstract fun marineDao(): MarineDao

    companion object {
        @Volatile
        private var INSTANCE: MarineDatabase? = null

        fun getDatabase(context: Context): MarineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarineDatabase::class.java,
                    "marine_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
