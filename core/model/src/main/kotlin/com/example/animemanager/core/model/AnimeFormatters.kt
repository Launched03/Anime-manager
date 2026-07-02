package com.example.animemanager.core.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun clampProgress(progressEpisode: Int, totalEpisodes: Int?): Int {
    val safeProgress = progressEpisode.coerceAtLeast(0)
    return totalEpisodes?.takeIf { it >= 0 }?.let { safeProgress.coerceAtMost(it) } ?: safeProgress
}

fun weekdayLabel(weekday: Int?): String = when (weekday) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    7 -> "周日"
    else -> "未设置"
}

fun minuteOfDayLabel(minuteOfDay: Int?): String {
    if (minuteOfDay == null || minuteOfDay !in 0..(24 * 60 - 1)) {
        return "未设置"
    }
    val hour = minuteOfDay / 60
    val minute = minuteOfDay % 60
    return LocalTime.of(hour, minute).format(timeFormatter)
}

fun scheduleLabel(weekday: Int?, minuteOfDay: Int?): String {
    val day = weekdayLabel(weekday)
    val time = minuteOfDayLabel(minuteOfDay)
    return if (day == "未设置" && time == "未设置") {
        "未设置更新时间"
    } else if (day == "未设置") {
        time
    } else if (time == "未设置") {
        day
    } else {
        "每$day $time"
    }
}

fun progressLabel(progressEpisode: Int, totalEpisodes: Int?): String {
    val safeProgress = progressEpisode.coerceAtLeast(0)
    return if (totalEpisodes != null && totalEpisodes >= 0) {
        "第${safeProgress}集 / 共${totalEpisodes}集"
    } else {
        "已看${safeProgress}集"
    }
}

fun seasonLabel(seasonYear: Int?, seasonName: String?): String {
    val yearText = seasonYear?.toString().orEmpty()
    val seasonText = seasonName.orEmpty()
    return when {
        yearText.isBlank() && seasonText.isBlank() -> "未设置"
        yearText.isBlank() -> seasonText
        seasonText.isBlank() -> yearText
        else -> "$yearText · $seasonText"
    }
}

fun premiereDateLabel(premiereDate: String?): String {
    val dateText = premiereDate?.trim().orEmpty()
    if (dateText.isBlank()) return "未设置"
    val date = runCatching { LocalDate.parse(dateText) }.getOrNull() ?: return dateText
    return "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
}

fun buildMinuteOfDay(hour: Int, minute: Int): Int {
    val safeHour = hour.coerceIn(0, 23)
    val safeMinute = minute.coerceIn(0, 59)
    return safeHour * 60 + safeMinute
}

fun splitMinuteOfDay(minuteOfDay: Int?): Pair<Int, Int>? {
    if (minuteOfDay == null || minuteOfDay !in 0..(24 * 60 - 1)) {
        return null
    }
    return minuteOfDay / 60 to minuteOfDay % 60
}

fun currentIsoWeekday(): Int = LocalDate.now().dayOfWeek.value

fun currentMinuteOfDay(): Int = LocalTime.now().hour * 60 + LocalTime.now().minute
