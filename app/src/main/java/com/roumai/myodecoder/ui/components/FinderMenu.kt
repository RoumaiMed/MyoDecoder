package com.roumai.myodecoder.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clj.fastble.data.BleDevice
import com.roumai.myodecoder.R

@Composable
fun FinderMenu(
    value: String,
    items: List<Pair<String, BleDevice>>,
    onFinding: (MutableState<Boolean>) -> Unit,
    onSelected: (
        MutableState<Boolean>, MutableState<Boolean>, MutableState<Boolean>, MutableState<Boolean>,
        Pair<String, BleDevice>
    ) -> Unit,
    onUnselected: (MutableState<Boolean>) -> Unit,
    backgroundColor: Color
) {
    val expanded = remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }
    val connectionState = remember { mutableStateOf(false) }
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
                            if (!connectionState.value) {
                                expanded.value = !expanded.value
                            }
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
                if (!connectionState.value) {
                    Icon(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation.value)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (!loading.value && expanded.value) {
                                        expanded.value = false
                                    }
                                    if (!loading.value && !connectionState.value) {
                                        onFinding(loading)
                                        if (loading.value) {
                                            expanded.value = true
                                        }
                                    }
                                }
                            ),
                        imageVector = Icons.Default.Refresh,
                        tint = Color.Black,
                        contentDescription = "finding..."
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.bt),
                        contentDescription = "connected",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onUnselected(connectionState)
                                }
                            ),
                    )
                }
            }
            DropdownMenu(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                expanded = expanded.value,
                onDismissRequest = {
                    expanded.value = false
                },
            ) {
                val oneClicked = remember { mutableStateOf(false) }
                for (item in items) {
                    val device = item.second
                    val clicked = remember { mutableStateOf(false) }
                    DropdownMenuItem(
                        modifier = Modifier
                            .height(32.dp)
                            .background(
                                if (clicked.value) Color.LightGray
                                else Color.Transparent
                            ),
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
                            if (oneClicked.value) {
                                return@DropdownMenuItem
                            }
                            oneClicked.value = true
                            clicked.value = true
                            onSelected(loading, clicked, expanded, connectionState, item)
                        }
                    )
                }
            }
        }
    }
}