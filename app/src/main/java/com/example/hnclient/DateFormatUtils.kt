package com.example.hnclient

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("DD.MM")
private val dateFormatterWithYear = DateTimeFormatter.ofPattern("DD.MM.yyyy")

fun formatTime(unixTime: Long): String {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(unixTime),
        ZoneId.of("UTC"),
    )
        .atZone(TimeZone.getDefault().toZoneId())
    val now = LocalDateTime.now()
    return when {
        dateTime.toLocalDate() == now.toLocalDate() -> "in ${dateTime.format(timeFormatter)}"
        dateTime.toLocalDate().plusDays(1) == now.toLocalDate() -> {
            "yesterday in ${dateTime.format(timeFormatter)}"
        }
        dateTime.toLocalDate().year == now.toLocalDate().year -> {
            "${dateTime.format(dateFormatter)} in ${dateTime.format(timeFormatter)}"
        }
        else -> "${dateTime.format(dateFormatterWithYear)} in ${dateTime.format(timeFormatter)}"
    }
}
