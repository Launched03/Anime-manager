package com.example.animemanager.core.data.repository

class DuplicateAnimeException(
    val existingAnimeId: Long,
    val existingTitle: String,
) : IllegalStateException("Duplicate anime: $existingTitle")
