package com.roumai.myodecoder.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.roumai.myodecoder.ui.components.CompassOption
import com.roumai.myodecoder.ui.components.GyroscopeOption
import com.roumai.myodecoder.ui.components.MotionOption
import com.roumai.myodecoder.ui.components.RTWindowOption
import com.roumai.myodecoder.ui.theme.COLOR_BACKGROUND
import com.roumai.myodecoder.ui.theme.ColorWhite

object GlobalConfig {
    const val SAMPLE_RATE = 1000
    const val CHANNEL_NUM = 1
    const val DATA_STORE_SIZE = 12000

    var rtWindowOption by mutableStateOf(RTWindowOption())
    var gyroscopeOption by mutableStateOf(GyroscopeOption(
        backgroundColor = COLOR_BACKGROUND,
        foregroundColor = ColorWhite
    ))
    var compassOption by mutableStateOf(CompassOption(
            backgroundColor = COLOR_BACKGROUND
        )
    )
    var motionOption by mutableStateOf(MotionOption(
        backgroundColor = COLOR_BACKGROUND
    ))

    var windowSize by mutableStateOf(2000) // int: ms
    var enableFiltering by mutableStateOf(false) // judge whether to filter 50HZ and its harmonic
}