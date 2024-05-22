package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.roumai.myodecoder.ui.theme.ColorDarkGray
import com.roumai.myodecoder.ui.theme.ColorGraphite
import com.roumai.myodecoder.ui.theme.ColorLightGray
import com.roumai.myodecoder.ui.theme.ColorWhite
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

data class RTWindowOption(
    val signalColor: Color = ColorWhite,
    val backgroundColor: Color = ColorGraphite,
    val packetLossColor: Color = ColorDarkGray,
    val padding: PaddingValues = PaddingValues(0.dp, 0.dp),
    val roundedSize: Dp = 6.dp,
    val boldLine: Boolean = true,
    val voltScale: Float = 1f,
    val voltOrigin: Float = 0f,
    val tickCount: Int = 5,
    val zeroPadding: Dp = 1.dp,
    val verticalPadding: Dp = 4.dp,
    val horizontalPadding: Dp = 4.dp,
    val voltRange: Float = 10f,
    val showPacketLoss: Boolean = true,
    val showXAxis: Boolean = true,
    val showYAxis: Boolean = true,
)

@Composable
fun RTWindow(
    modifier: Modifier,
    channelIdx: Int,
    data: MutableList<Pair<Long, FloatArray>>,
    options: RTWindowOption
) {
    val path = remember { Path() }
    val xAxis = remember { Path() }
    val yAxis = remember { Path() }
    val voltScale = options.voltScale
    val voltOrigin = options.voltOrigin
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(options.roundedSize))
                .background(color = options.backgroundColor)
                .clipToBounds()
                .fillMaxSize()
        ) {
            if (data.isEmpty()) return@Canvas
            val yMiddle = size.height / 2
            val graphHeight = size.height - 2 * options.verticalPadding.toPx()
            val graphWidth = size.width - 2 * options.horizontalPadding.toPx() - options.zeroPadding.toPx()
            val dense = data.size / graphWidth
            val volt0 = data[0].second[channelIdx] * voltScale + voltOrigin
            val y0 = yMiddle + volt0 / options.voltRange * graphHeight / 2
            path.reset()
            path.moveTo(options.horizontalPadding.toPx() + options.zeroPadding.toPx(), y0)
            data.forEach { (ts, voltages) ->
                val x = (ts - data[0].first) / dense + options.horizontalPadding.toPx() + options.zeroPadding.toPx()
                val volt = voltages[channelIdx] * voltScale + voltOrigin
                val y = yMiddle + volt / options.voltRange * graphHeight / 2
                path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = options.signalColor,
                style = Stroke(
                    width = if (options.boldLine) 1.2.dp.toPx() else 0.5.dp.toPx(),
                )
            )
            xAxis.reset()
            if (options.showXAxis) {
                val tickCnt = if (data.size > options.tickCount) options.tickCount else data.size
                xAxis.moveTo(options.horizontalPadding.toPx(), yMiddle)
                xAxis.lineTo(size.width - options.horizontalPadding.toPx(), yMiddle)
                drawPath(
                    path = xAxis,
                    color = ColorLightGray,
                    style = Stroke(width = 0.5.dp.toPx())
                )
                val xTickStep = graphWidth / tickCnt
                for (i in 0 until tickCnt) {
                    val x = options.horizontalPadding.toPx() + options.zeroPadding.toPx() + i * xTickStep
                    drawLine(
                        start = Offset(x, yMiddle - 2.dp.toPx()),
                        end = Offset(x, yMiddle + 2.dp.toPx()),
                        color = ColorLightGray,
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }
            if (options.showYAxis) {
                yAxis.reset()
                yAxis.moveTo(options.horizontalPadding.toPx(), options.verticalPadding.toPx())
                yAxis.lineTo(options.horizontalPadding.toPx(), size.height - options.verticalPadding.toPx())
                drawPath(
                    path = yAxis,
                    color = ColorLightGray,
                    style = Stroke(
                        width = 0.5.dp.toPx(),
                    )
                )
            }
        }
    }
}

fun generateMockData(length: Int): List<Pair<Long, FloatArray>> {
    val startTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
    return List(length) { i ->
        val timestamp = startTime + i
        val arr = floatArrayOf(Random.nextDouble(-10.0, 10.0).toFloat())
        Pair(timestamp, arr)
    }
}

@Preview
@Composable
fun RTWindowPreview() {
    val data = generateMockData(100)
    Box(modifier = Modifier.size(width = 360.dp, height = 200.dp)) {
        RTWindow(
            modifier = Modifier
                .fillMaxSize(),
            0,
            data.toMutableList(),
            RTWindowOption()
        )
    }
}