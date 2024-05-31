package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SciBox(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp,
    backgroundColor: Color,
    content: @Composable () -> Unit,
) {
    val gradientColors = listOf(Color(0xFF008080), Color(0xFF00008B))
    Box(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .drawBehind {
                val shadowRadius = 20.dp.toPx()
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = backgroundColor.toArgb()
                        setShadowLayer(shadowRadius, 0f, 0f, Color.Cyan.toArgb())
                    }
                }
                val gradient = Brush.horizontalGradient(gradientColors)
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(
                        0f,
                        0f,
                        size.width,
                        size.height,
                        8.dp.toPx(),
                        8.dp.toPx(),
                        paint
                    )
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            content.invoke()
        }
    }
}