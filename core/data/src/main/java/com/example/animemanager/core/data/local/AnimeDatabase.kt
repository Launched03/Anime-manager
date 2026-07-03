package com.example.animemanager.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.animemanager.core.data.local.dao.AnimeDao
import com.example.animemanager.core.data.local.entity.AnimeEntity
import com.example.animemanager.core.data.local.entity.AnimeScheduleEntity
import com.example.animemanager.core.data.local.entity.UserAnimeStateEntity

@Database(
    entities = [AnimeEntity::class, AnimeScheduleEntity::class, UserAnimeStateEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}
