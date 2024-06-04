package com.roumai.myodecoder.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


fun getSciTextStyle(
    color: Color,
    fontSize: Float,
): TextStyle {
    return TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Light,
        shadow = Shadow(
            color = color.copy(alpha = 1f),
            offset = Offset(0f, 0f),
            blurRadius = 5f
        )
    )
}

@Composable
fun SciText(
    text: String,
    color: Color,
    fontSize: Float,
) {
    Text(
        text = text,
        style = getSciTextStyle(color, fontSize)
    )
}