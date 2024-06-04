package com.roumai.myodecoder.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalConfig {
    const val SAMPLE_RATE = 1000
    const val CHANNEL_NUM = 1
    const val DATA_STORE_SIZE = 12000

    var windowSize by mutableStateOf(5000) // int: ms
}