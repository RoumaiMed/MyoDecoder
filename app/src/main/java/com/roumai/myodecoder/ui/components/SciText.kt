package com.roumai.myodecoder.ui.components

import android.graphics.Color.parseColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SciText(
    text: String,
    colorHex: String,
    fontSize: Float,
) {
    val color = Color(parseColor(colorHex))
    Text(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Light,
            shadow = Shadow(
                color = color.copy(alpha = 1f),
                offset = Offset(0f, 0f),
                blurRadius = 5f
            )
        )
    )
}