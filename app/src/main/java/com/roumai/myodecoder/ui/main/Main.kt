package com.roumai.myodecoder.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.clj.fastble.data.BleDevice
import com.roumai.myodecoder.R
import com.roumai.myodecoder.device.ble.MyoBleFinder
import com.roumai.myodecoder.ui.components.FinderMenu

@Composable
fun Main() {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(context.getString(R.string.key_select_devices)) }
    val devices = mutableListOf<Pair<String, BleDevice>>()
    val finder = MyoBleFinder(true)
    FinderMenu(
        value = selected,
        items = devices,
        onFinding = {
            devices.clear()
            finder.enableDebug(true)
            finder.scan(object : MyoBleFinder.OnFinderUpdate {
                override fun onStart() {
                    it.value = true
                }

                override fun onFound(peripheral: BleDevice) {
                    devices.add(peripheral.mac to peripheral)
                }

                override fun onStop(peripherals: List<BleDevice>) {
                    println("stop")
                    it.value = false
                }
            })
        },
        onSelected = {
            selected = it.first
        },
        backgroundColor = Color(0xFFE0E0E0)
    )
}