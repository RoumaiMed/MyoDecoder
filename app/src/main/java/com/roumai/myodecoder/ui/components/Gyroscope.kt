package com.roumai.myodecoder.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roumai.myodecoder.ui.theme.ColorSciBlue
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.sqrt

data class GyroscopeOption(
    val backgroundColor: Color = Color.DarkGray,
    val foregroundColor: Color = Color.White,
)


@OptIn(ExperimentalTextApi::class)
@Composable
fun Gyroscope(
    modifier: Modifier,
    data: Triple<Float, Float, Float>,
    options: GyroscopeOption
) {
    val path = remember { Path() }
    val textMeasurer = rememberTextMeasurer()
    Box(modifier = modifier) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .background(options.backgroundColor)
        ) {
            val radius = min(size.height, size.width) / 8
            val x = data.third
            val y = data.second
            val z = data.first

            // [-90, 90] -> [0, width]
            val center1 = Offset(
                x = (x + 90) / 180 * size.width,
                y = (y + 90) / 180 * size.height,
            )
            val center2 = Offset(
                x = size.width - (x + 90) / 180 * size.width,
                y = size.height - (y + 90) / 180 * size.height,
            )

            // Clip to the canvas size to ensure circles don't draw outside the canvas
            clipRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height
            ) {
                drawCircle(
                    color = options.foregroundColor,
                    radius = radius,
                    center = center1,
                )
                drawCircle(
                    color = options.foregroundColor,
                    radius = radius,
                    center = center2,
                )
            }

            // Draw intersection
            val tempPath1 = Path().apply {
                addOval(
                    Rect(radius = radius, center = center1)
                )
            }

            val tempPath2 = Path().apply {
                addOval(
                    Rect(radius = radius, center = center2)
                )
            }

            val diffPath = Path.combine(
                operation = PathOperation.Intersect,
                path1 = tempPath1,
                path2 = tempPath2,
            )

            path.reset()
            path.addPath(diffPath)
            drawPath(path, color = options.backgroundColor)

            // Draw text
            val deltaX = (center1.x - center2.x)
            val rotation =
                if (deltaX == 0f) 0f else (atan((center1.y - center2.y) / deltaX) + PI / 2).toFloat()

            rotate(degrees = rotation * 180 / PI.toFloat(), pivot = center) {
                val degree = sqrt(x * x + y * y)
                val offset = (if (degree < 10) 18 else 28) + (if (z < 0) 0 else -6)
                val textStyle = getSciTextStyle(ColorSciBlue, 32f)
                drawText(
                    textMeasurer,
                    text = "${if (z < 0) "-" else ""}${degree.toInt()}˚",
                    topLeft = center - Offset(offset.dp.toPx(), 22.dp.toPx()),
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                    style = textStyle
                )
            }
        }
    }
}


@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun GyroscopePreview() {
    Gyroscope(
        modifier = Modifier.size(200.dp, 200.dp),
        data = Triple(1f, 0f, 20f),
        options = GyroscopeOption()
    )
}
