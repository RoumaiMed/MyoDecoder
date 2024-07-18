package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.roumai.myodecoder.ui.theme.ColorGraphite
import com.roumai.myodecoder.ui.theme.ColorGray
import com.roumai.myodecoder.ui.theme.ColorLightGray
import com.roumai.myodecoder.ui.theme.ColorWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

data class RTWindowOption(
    val signalColor: Color = ColorWhite,
    val backgroundColor: Color = ColorGraphite,
    val packetLossColor: Color = ColorGray,
    val padding: PaddingValues = PaddingValues(0.dp, 0.dp),
    val roundedSize: Dp = 6.dp,
    val boldLine: Boolean = true,
    val tickCount: Int = 5,
    val verticalPadding: Dp = 4.dp,
    val horizontalPadding: Dp = 4.dp,
    val voltRange: Float = 3.3f,
    val showPacketLoss: Boolean = true,
    val showXAxis: Boolean = true,
    val showYAxis: Boolean = true,
    var voltScale: MutableState<Float> = mutableStateOf(1f),
)

@Composable
fun RTWindow(
    modifier: Modifier,
    data: List<Pair<Long, Float?>>,
    options: RTWindowOption
) {
    val path = remember { Path() }
    val lossPath = remember { Path() }
    val xAxis = remember { Path() }
    val yAxis = remember { Path() }
    val voltScale = options.voltScale
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(options.roundedSize))
                .background(color = options.backgroundColor)
                .clipToBounds()
                .fillMaxSize()
        ) {
            if (data.size <= 1) return@Canvas
            path.reset()
            lossPath.reset()
            xAxis.reset()
            yAxis.reset()
            val yMiddle = size.height / 2
            val graphHeight = size.height - 2 * options.verticalPadding.toPx()
            val graphWidth = size.width - 2 * options.horizontalPadding.toPx()
            val dense = data.size / graphWidth
            var pathStarted = false
            val initialX = options.horizontalPadding.toPx()
            var lastValidX = 0
            val lineStyle = Stroke(
                width = if (options.boldLine) 1.2.dp.toPx() else 0.5.dp.toPx(),
            )
            data.forEachIndexed { idx, (ts, voltage) ->
                val x = (ts - data[0].first) / dense + initialX
                if (voltage != null) {
                    val volt = voltage * voltScale.value
                    val y = yMiddle + volt / options.voltRange * graphHeight / 2
                    if (!pathStarted) {
                        path.moveTo(x, y)
                        pathStarted = true
                    } else {
                        path.lineTo(x, y)
                    }
                    if (lastValidX != 0 && idx - lastValidX > 1) {
                        drawPath(
                            path = lossPath.apply { lineTo(x, y) },
                            color = options.signalColor.copy(alpha = 0.2f),
                            style = lineStyle
                        )
                        lossPath.reset()
                    }
                    lossPath.moveTo(x, y)
                    lastValidX = idx
                    if ((idx == 0 && data[1].second == null) ||
                        (idx == data.size - 1 && data[data.size - 2].second == null) ||
                        (idx != 0 && idx != data.size - 1 &&
                                data[idx - 1].second == null && data[idx + 1].second == null)
                    ) {
                        drawCircle(
                            color = options.signalColor,
                            center = Offset(x, y),
                            radius = 1.dp.toPx()
                        )
                    }
                } else {
                    if (pathStarted) {
                        drawPath(
                            path = path,
                            color = options.signalColor,
                            style = lineStyle
                        )
                        path.reset()
                        pathStarted = false
                    }
                    if (options.showPacketLoss) {
                        val rectWidth = graphWidth / data.size
                        drawRect(
                            color = options.packetLossColor,
                            topLeft = Offset(x - rectWidth / 2, yMiddle - graphHeight / 2),
                            size = Size(rectWidth, graphHeight)
                        )
                    }
                }
            }
            if (pathStarted) {
                drawPath(
                    path = path,
                    color = options.signalColor,
                    style = lineStyle
                )
            }

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
                    val x = options.horizontalPadding.toPx() + i * xTickStep
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
                yAxis.lineTo(
                    options.horizontalPadding.toPx(),
                    size.height - options.verticalPadding.toPx()
                )
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

fun generateMockData(length: Int): List<Pair<Long, Float?>> {
    val startTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

    val array = ArrayList<Pair<Long, Float?>>()
    var lastY = 0f
    for (index in 0 until length) {
        lastY += Random.nextDouble(-1.0, 1.0).toFloat()
        lastY %= 10
        if (index % 2 == 0) {
            array.add(Pair(startTime + index, null))
        } else {
            array.add(Pair(startTime + index, lastY))
        }
    }
    return array
}

@Preview
@Composable
fun RTWindowPreview() {
    var data by remember {
        mutableStateOf(generateMockData(1000))
    }

    Box(modifier = Modifier.size(width = 360.dp, height = 200.dp)) {
        RTWindow(
            modifier = Modifier
                .fillMaxSize(),
            data,
            RTWindowOption()
        )
    }
}