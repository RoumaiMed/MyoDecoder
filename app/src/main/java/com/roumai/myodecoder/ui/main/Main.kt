package com.roumai.myodecoder.ui.main

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.clj.fastble.data.BleDevice
import com.roumai.myodecoder.R
import com.roumai.myodecoder.device.ble.MyoBleFinder
import com.roumai.myodecoder.device.ble.MyoBleService
import com.roumai.myodecoder.device.ble.impl.BleDelegateDefaultImpl
import com.roumai.myodecoder.ui.components.FinderMenu
import com.roumai.myodecoder.ui.utils.ToastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun Main(finder: MyoBleFinder?) {
    BleFinderMenu(
        finder = finder,
        onDeviceConnected = {
            CoroutineScope(Dispatchers.IO).launch {
                it.observeEMG {

                }
                it.observeIMU {

                }
                it.observeRMS {

                }
            }
        }
    )
}


@Composable
fun BleFinderMenu(
    finder: MyoBleFinder?,
    onDeviceConnected: (MyoBleService) -> Unit
) {
    val context = LocalContext.current
    var selected by remember {
        mutableStateOf(
            Pair<String, BleDevice?>(
                context.getString(R.string.key_select_devices),
                null
            )
        )
    }
    val connectionState = remember { mutableStateOf(false) }
    val devices = mutableListOf<Pair<String, BleDevice>>()
    FinderMenu(
        value = selected.first,
        items = devices,
        onFinding = {
            if (finder?.isBluetoothEnabled() == false) {
                ToastManager.showToast(context, context.getString(R.string.key_enable_bluetooth))
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
                    if (it.value) {
                        it.value = false
                    }
                }
            })
        },
        onSelected = { loading, clicked, expanded, it ->
            loading.value = false
            val delegate = BleDelegateDefaultImpl(it.second)
            val service = MyoBleService(delegate)
            CoroutineScope(Dispatchers.Main).launch {
                if (service.connect()) {
                    connectionState.value = true
                    selected = it
                    onDeviceConnected(service)
                } else {
                    connectionState.value = false
                    ToastManager.showToast(context, context.getString(R.string.key_connect_fail))
                }
                clicked.value = false
                expanded.value = false
            }
        },
        connectionState = connectionState,
        backgroundColor = Color(0xFFE0E0E0)
    )
}