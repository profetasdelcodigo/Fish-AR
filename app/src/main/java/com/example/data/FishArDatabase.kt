package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [FairLeaderboardEntity::class],
  version = 2,
  exportSchema = false
)
abstract class FishArDatabase : RoomDatabase() {
  abstract fun fairLeaderboardDao(): FairLeaderboardDao

  companion object {
    @Volatile private var INSTANCE: FishArDatabase? = null

    fun getInstance(context: Context): FishArDatabase =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          FishArDatabase::class.java,
          "fish_ar.db"
        ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
      }
  }
}
