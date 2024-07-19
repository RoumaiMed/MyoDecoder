package com.roumai.myodecoder.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.roumai.myodecoder.ui.theme.ColorBlack
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import java.lang.Math.toDegrees
import kotlin.math.cos
import kotlin.math.sin

data class MotionOption(
    val backgroundColor: Color = ColorBlack,
)

@Composable
fun Motion(
    modifier: Modifier,
    data: List<Float>,
    options: MotionOption,
) {
    // define X: forward, Y: left, Z: up, (USB port is forward)
    // 16-channel wristband
//    val accX = data[1].toDouble()
//    val accY = data[0].toDouble()
//    val accZ = -data[2].toDouble()
//    val magX = data[7]
//    val magY = data[6]
//    val magZ = data[8]

    // 1-channel wristband
    val accX = -data[1].toDouble()
    val accY = data[0].toDouble()
    val accZ = data[2].toDouble()
    val magX = -data[7]
    val magY = data[6]
    val magZ = -data[8]

    // pitch forward is positive, roll right is positive
    val pitchRadian = atan2(-accX, sqrt(accY.pow(2) + accZ.pow(2)))
    val rollRadian = atan2(accY, accZ)
    val pitch = toDegrees(pitchRadian)
    val roll = toDegrees(rollRadian)

    // automatic calibration of the compass
    val minMagX = remember { mutableStateOf(10000f) }
    val maxMagX = remember { mutableStateOf(-10000f) }
    updateMinMax(magX, minMagX, maxMagX)

    val minMagY = remember { mutableStateOf(10000f) }
    val maxMagY = remember { mutableStateOf(-10000f) }
    updateMinMax(magY, minMagY, maxMagY)

    val minMagZ = remember { mutableStateOf(10000f) }
    val maxMagZ = remember { mutableStateOf(-10000f) }
    updateMinMax(magZ, minMagZ, maxMagZ)

    val calibMagX = magX - minMagX.value - (maxMagX.value - minMagX.value) / 2f
    val calibMagY = magY - minMagY.value - (maxMagY.value - minMagY.value) / 2f
    val calibMagZ = magZ - minMagZ.value - (maxMagZ.value - minMagZ.value) / 2f

    // compensate compass with pitch and roll
    val magXCompensated = calibMagX * cos(pitchRadian) + calibMagZ * sin(pitchRadian)
    val magYCompensated = calibMagX * sin(rollRadian) * sin(pitchRadian) +
            calibMagY * cos(rollRadian) -
            calibMagZ * sin(rollRadian) * cos(pitchRadian)

    // moving average of azimuth
    val windowSize = 5
    val azimuthQueue = remember { ArrayDeque<Float>(windowSize) }
    val azimuth = toDegrees(atan2(magYCompensated, magXCompensated)).toFloat()
    if (azimuthQueue.size >= windowSize) {
        azimuthQueue.removeFirst()
    }
    azimuthQueue.addLast(azimuth)
    val filteredAzimuth = average(azimuthQueue)

    Box(
        modifier = Modifier
            .size(300.dp)
            .rotate(-filteredAzimuth - 90f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCompass()
        }
    }

    Box(
        modifier = Modifier
            .size(120.dp, 150.dp)
            .graphicsLayer(rotationX = pitch.toFloat(), rotationY = roll.toFloat(), rotationZ = 0f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Cyan)
    ) {
        Text(
            text = "X: %4.1f°\nY: %4.1f°\nZ: %4.1f°".format(pitch.toFloat(), roll.toFloat(), -filteredAzimuth - 90f),
            style = TextStyle(fontSize = 15.sp),
            modifier = Modifier
                .size(100.dp, 80.dp)
                .background(Color.LightGray)
                .padding(12.dp)
                .align(Alignment.Center)
        )
    }
}

fun updateMinMax(
    value: Float,
    minState: MutableState<Float>,
    maxState: MutableState<Float>,
    threshold: Float = 1f
) {
    if (value > maxState.value && (value > threshold || value < -threshold)) {
        maxState.value = value
    }
    if (value < minState.value && (value > threshold || value < -threshold)) {
        minState.value = value
    }
}

fun average(queue: ArrayDeque<Float>): Float {
    return queue.sum() / queue.size
}

fun DrawScope.drawCompass() {
    val radius = size.minDimension / 2
    val centerX = size.width / 2
    val centerY = size.height / 2

    drawCircle(
        color = Color.White,
        radius = radius,
        center = center,
        style = Stroke(
            width = 2f
        )
    )

    // paint for text
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    // directions and angles
    val directions = listOf(
        "N" to 0f, "NE" to 45f, "E" to 90f, "SE" to 135f,
        "S" to 180f, "SW" to 225f, "W" to 270f, "NW" to 315f
    )

    directions.forEach { (label, angle) ->
        val angleInRadians = Math.toRadians(angle.toDouble())
        val x = (centerX + (radius - 60) * cos(angleInRadians)).toFloat()
        val y = (centerY + (radius - 60) * sin(angleInRadians)).toFloat() + 15
        drawContext.canvas.nativeCanvas.drawText(label, x, y, paint)
    }

    // draw angles at 30-degree intervals
    val anglePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 20f
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }

    for (angle in 0 until 360 step 30) {
        if (angle % 90 != 0) { // Skip the main directions already labeled
            val angleInRadians = Math.toRadians(angle.toDouble())
            val x = (centerX + (radius - 40) * cos(angleInRadians)).toFloat()
            val y = (centerY + (radius - 40) * sin(angleInRadians)).toFloat() + 7
            drawContext.canvas.nativeCanvas.drawText(angle.toString(), x, y, anglePaint)
        }
    }
}

@Preview
@Composable
fun MotionPreview() {
    Box(modifier = Modifier.size(360.dp, 360.dp)) {
        Motion(
            modifier = Modifier.fillMaxSize(),
            data = List(9) { 0.1f },
            options = MotionOption()
        )
    }
}