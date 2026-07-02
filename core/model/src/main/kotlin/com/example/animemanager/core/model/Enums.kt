package com.example.animemanager.core.model

enum class SeriesStatus(val dbValue: Int) {
    UPCOMING(0),
    ONGOING(1),
    FINISHED(2);

    fun displayLabel(): String = when (this) {
        UPCOMING -> "未开播"
        ONGOING -> "连载中"
        FINISHED -> "已完结"
    }

    companion object {
        fun fromDbValue(value: Int): SeriesStatus = entries.firstOrNull { it.dbValue == value } ?: ONGOING
    }
}

enum class WatchState(val dbValue: Int) {
    WANT_TO_WATCH(0),
    WATCHING(1),
    WATCHED(2),
    HOLD(3);

    fun displayLabel(): String = when (this) {
        WANT_TO_WATCH -> "想看"
        WATCHING -> "在看"
        WATCHED -> "已看"
        HOLD -> "搁置"
    }

    companion object {
        fun fromDbValue(value: Int?): WatchState? = entries.firstOrNull { it.dbValue == value }
    }
}

enum class AnimeSortOrder(val dbValue: Int) {
    TITLE_ASC(0),
    UPDATED_DESC(1),
    NEXT_UPDATE_ASC(2),
    STATUS_THEN_TITLE(3);

    fun displayLabel(): String = when (this) {
        TITLE_ASC -> "名称"
        UPDATED_DESC -> "最近更新"
        NEXT_UPDATE_ASC -> "下次更新"
        STATUS_THEN_TITLE -> "状态优先"
    }

    companion object {
        fun fromDbValue(value: Int): AnimeSortOrder = entries.firstOrNull { it.dbValue == value } ?: TITLE_ASC
    }
}

enum class ThemeMode(val dbValue: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    fun displayLabel(): String = when (this) {
        SYSTEM -> "跟随系统"
        LIGHT -> "浅色"
        DARK -> "深色"
    }

    companion object {
        fun fromDbValue(value: Int): ThemeMode = entries.firstOrNull { it.dbValue == value } ?: SYSTEM
    }
}
