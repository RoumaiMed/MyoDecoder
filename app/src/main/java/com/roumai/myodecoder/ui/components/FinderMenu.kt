package com.roumai.myodecoder.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clj.fastble.data.BleDevice

@Composable
fun FinderMenu(
    value: String,
    items: List<Pair<String, BleDevice>>,
    onFinding: (MutableState<Boolean>) -> Unit,
    onSelected: (Pair<String, BleDevice>) -> Unit,
    backgroundColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }
    val rotation = if (loading.value) {
        val infiniteTransition = rememberInfiniteTransition(label = "transition")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = LinearEasing,
                ),
            ), label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(backgroundColor)
            ) {
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(0.9f)
                        .clickable(onClick = {
                            expanded = !expanded
                        }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        value,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                expanded = true
                                onFinding(loading)
                            }),
                    imageVector = Icons.Default.Refresh,
                    tint = Color.Black,
                    contentDescription = "finding..."
                )
            }
            DropdownMenu(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
            ) {
                for (item in items) {
                    val device = item.second
                    DropdownMenuItem(
                        modifier = Modifier.height(32.dp),
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    device.name ?: "Unknown",
                                    fontSize = 14.sp,
                                )
                                Text(
                                    device.mac,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelected(item)
                        }
                    )
                }
            }
        }
    }
}