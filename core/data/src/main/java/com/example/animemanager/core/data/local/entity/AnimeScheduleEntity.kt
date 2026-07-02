package com.example.animemanager.core.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "anime_schedule",
    foreignKeys = [
        ForeignKey(
            entity = AnimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["anime_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class AnimeScheduleEntity(
    @PrimaryKey
    @ColumnInfo(name = "anime_id")
    val animeId: Long,
    val weekday: Int? = null,
    val minuteOfDay: Int? = null,
    val timezone: String? = null,
    val isActive: Boolean = false,
)
