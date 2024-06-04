package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.roumai.myodecoder.core.GlobalConfig
import com.roumai.myodecoder.ui.theme.ColorWhite

@Composable
fun ZoomTime() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.2f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("%.3fs".format(GlobalConfig.windowSize.toFloat() / 1000f), color = ColorWhite)
        }
        Slider(
            modifier = Modifier.fillMaxWidth(0.8f),
            value = GlobalConfig.windowSize.toFloat() / 1000f,
            valueRange = 0.1f..10f,
            onValueChange = {
                GlobalConfig.windowSize = (it * 1000).toInt()
            },
            colors = SliderDefaults.colors(
                thumbColor = ColorWhite,
                activeTickColor = ColorWhite,
                activeTrackColor = ColorWhite,
            ),
        )
    }
}