package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSpacer(
    height: Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    )
}