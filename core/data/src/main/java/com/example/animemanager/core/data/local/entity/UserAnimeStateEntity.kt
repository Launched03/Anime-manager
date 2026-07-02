package com.example.animemanager.core.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_anime_state",
    foreignKeys = [
        ForeignKey(
            entity = AnimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["anime_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class UserAnimeStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "anime_id")
    val animeId: Long,
    val watchState: Int? = null,
    val progressEpisode: Int = 0,
    val isFavorite: Boolean = false,
    val score: Int? = null,
    val note: String? = null,
    val updatedAt: Long,
)
