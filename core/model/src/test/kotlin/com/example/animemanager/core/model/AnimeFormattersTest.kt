package com.example.animemanager.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnimeFormattersTest {
    @Test
    fun clampProgress_neverGoesBelowZero() {
        assertEquals(0, clampProgress(-3, null))
    }

    @Test
    fun clampProgress_respectsTotalEpisodes() {
        assertEquals(12, clampProgress(18, 12))
    }

    @Test
    fun weekdayLabel_formatsKnownDays() {
        assertEquals("周一", weekdayLabel(1))
        assertEquals("周日", weekdayLabel(7))
    }

    @Test
    fun minuteOfDayLabel_formatsTime() {
        assertEquals("23:30", minuteOfDayLabel(23 * 60 + 30))
        assertEquals("未设置", minuteOfDayLabel(null))
    }

    @Test
    fun scheduleLabel_combinesDayAndTime() {
        assertEquals("每周五 23:30", scheduleLabel(5, 23 * 60 + 30))
        assertEquals("未设置更新时间", scheduleLabel(null, null))
    }

    @Test
    fun progressLabel_formatsKnownTotal() {
        assertEquals("第18集 / 共28集", progressLabel(18, 28))
    }

    @Test
    fun premiereDateLabel_formatsIsoDate() {
        assertEquals("2026年4月12日", premiereDateLabel("2026-04-12"))
        assertEquals("未设置", premiereDateLabel(null))
    }

    @Test
    fun enumConverters_fallbackSafely() {
        assertEquals(SeriesStatus.ONGOING, SeriesStatus.fromDbValue(999))
        assertEquals(WatchState.WATCHING, WatchState.fromDbValue(1))
        assertNull(WatchState.fromDbValue(null))
        assertEquals(AnimeSortOrder.TITLE_ASC, AnimeSortOrder.fromDbValue(999))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromDbValue(999))
    }
}
