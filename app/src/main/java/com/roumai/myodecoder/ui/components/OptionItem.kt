package com.roumai.myodecoder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.roumai.myodecoder.ui.theme.ColorWhite

@Composable
fun OptionItem(
    text: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = text, color = ColorWhite)
        }
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            horizontalArrangement = Arrangement.Center,
        ) {
            var checked by remember { mutableStateOf(false) }
            Switch(
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    checked = it
                },
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}