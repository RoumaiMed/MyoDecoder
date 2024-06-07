package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roumai.myodecoder.R
import com.roumai.myodecoder.ui.theme.ColorBlack
import kotlin.math.cos
import kotlin.math.sin

data class CompassOption(
    val backgroundColor: Color = ColorBlack,
)

@Composable
fun Compass(
    modifier: Modifier,
    data: Float,
    options: CompassOption,
) {
    val angleRad = data * Math.PI / 180
    Box(modifier = modifier) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .background(options.backgroundColor),
            painter = painterResource(id = R.drawable.compass),
            contentDescription = "compass"
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size.height
                val circleSize = canvasSize * 0.035f
                val radius = canvasSize * 0.37f
                val offsetX = (radius * cos(angleRad)).toFloat()
                val offsetY = (radius * sin(angleRad)).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = circleSize,
                    center = Offset(size.width / 2 - offsetX, size.height / 2 - offsetY)
                )
            }
        }
    }
}

@Preview
@Composable
fun CompassPreview() {
    Box(modifier = Modifier.size(360.dp, 360.dp)) {
        Compass(
            modifier = Modifier.fillMaxSize(),
            data = 0f,
            options = CompassOption()
        )
    }
}