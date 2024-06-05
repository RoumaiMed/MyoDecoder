package com.roumai.myodecoder.core

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeConvertor {
    fun getTime(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val instant = Instant.ofEpochMilli(currentTimeMillis)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
        val formattedDate = dateTime.format(formatter)
        return formattedDate
    }
}