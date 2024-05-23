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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinderMenu(
    value: String,
    items: List<Pair<String, Any>>,
    onFinding: (MutableState<Boolean>) -> Unit,
    onSelected: (Pair<String, Any>) -> Unit,
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
                    DropdownMenuItem(
                        modifier = Modifier.height(32.dp),
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    item.first,
                                    fontSize = 14.sp,
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