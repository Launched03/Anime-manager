package com.example.animemanager.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: String? = null,
    val title: String,
    val originalTitle: String? = null,
    val posterRef: String? = null,
    val synopsis: String? = null,
    val seriesStatus: Int,
    val totalEpisodes: Int? = null,
    val updatedEpisodes: Int? = null,
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val premiereDate: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
