package com.roumai.myodecoder.ui.main

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.clj.fastble.data.BleDevice
import com.roumai.myodecoder.R
import com.roumai.myodecoder.device.ble.MyoBleFinder
import com.roumai.myodecoder.ui.components.FinderMenu
import com.roumai.myodecoder.ui.utils.ToastManager

@Composable
fun Main(finder: MyoBleFinder?) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(context.getString(R.string.key_select_devices)) }
    val devices = mutableListOf<Pair<String, BleDevice>>()
    FinderMenu(
        value = selected,
        items = devices,
        onFinding = {
            if (finder?.isBluetoothEnabled() == false) {
                ToastManager.showToast(context, "Please enable Bluetooth first.")
                return@FinderMenu
            }
            devices.clear()
            finder?.enableDebug(true)
            finder?.scan(object : MyoBleFinder.OnFinderUpdate {
                override fun onStart() {
                    it.value = true
                }

                override fun onFound(peripheral: BleDevice) {
                    devices.add(peripheral.mac to peripheral)
                }

                override fun onStop(peripherals: List<BleDevice>) {
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