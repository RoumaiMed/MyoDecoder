package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.roumai.myodecoder.core.GlobalConfig
import com.roumai.myodecoder.ui.theme.ColorWhite

@Composable
fun ZoomTime() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.18f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("%.3fs".format(GlobalConfig.windowSize.toFloat() / 1000f), color = ColorWhite)
        }
        Slider(
            modifier = Modifier.fillMaxWidth(0.62f),
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
        Row(
            modifier = Modifier.fillMaxWidth(0.2f),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier.fillMaxSize(),
                onClick = { GlobalConfig.windowSize = 5000 }
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "refresh",
                    tint = ColorWhite
                )
            }
        }
    }
}

@Composable
fun ZoomScale() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.18f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("×%.3f".format(GlobalConfig.rtWindowOption.voltScale.value), color = ColorWhite)
        }
        Slider(
            modifier = Modifier.fillMaxWidth(0.62f),
            value = GlobalConfig.rtWindowOption.voltScale.value,
            valueRange = 0.1f..10f,
            onValueChange = {
                GlobalConfig.rtWindowOption.voltScale.value = it
            },
            colors = SliderDefaults.colors(
                thumbColor = ColorWhite,
                activeTickColor = ColorWhite,
                activeTrackColor = ColorWhite,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.2f),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier.fillMaxSize(),
                onClick = { GlobalConfig.rtWindowOption.voltScale.value = 1f }
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "refresh",
                    tint = ColorWhite
                )
            }
        }
    }
}