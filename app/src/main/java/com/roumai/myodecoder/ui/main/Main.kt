package com.roumai.myodecoder.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.clj.fastble.data.BleDevice
import com.roumai.myodecoder.R
import com.roumai.myodecoder.core.DataManager
import com.roumai.myodecoder.core.GlobalConfig
import com.roumai.myodecoder.device.ble.MyoBleFinder
import com.roumai.myodecoder.device.ble.MyoBleService
import com.roumai.myodecoder.device.ble.impl.BleDelegateDefaultImpl
import com.roumai.myodecoder.ui.components.*
import com.roumai.myodecoder.ui.theme.COLOR_BACKGROUND
import com.roumai.myodecoder.ui.theme.ColorSciBlue
import com.roumai.myodecoder.ui.theme.ColorWhite
import com.roumai.myodecoder.ui.utils.ToastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun Main(
    finder: MyoBleFinder?
) {
    val context = LocalContext.current
    val emgDataState = remember { mutableStateOf<List<Pair<Long, Float?>>>(emptyList()) }
    val gyroDataState = remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    val angleState = remember { mutableStateOf(90f) }
    val imu9DataState = remember { mutableStateOf(List(9) { 0.1f }) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(COLOR_BACKGROUND)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val config = LocalConfiguration.current
        val horizontalPadding = 12.dp
        VerticalSpacer(height = 20.dp)
        BleFinderMenu(
            finder = finder,
            onDeviceConnected = {
                DataManager.startService(
                    it,
                    onEmgCallback = { emg -> emgDataState.value = emg },
                    onGyroCallback = { gyro -> gyroDataState.value = gyro },
                    onAngleCallback = { angle -> angleState.value = angle },
                    onImu9Callback = { imu9 -> imu9DataState.value = imu9 }
                )
            },
            onDeviceDisconnected = {
                DataManager.removeService()
            },
        )
        VerticalSpacer(height = 40.dp)
        val boxWidth = config.screenWidthDp.dp - horizontalPadding * 2
//        Box(
//            modifier = Modifier
//                .padding(horizontal = horizontalPadding)
//                .width(boxWidth)
//                .height(boxWidth)
//        ) {
//            GyroWindow(
//                modifier = Modifier
//                    .fillMaxSize(),
//                data = gyroDataState.value,
//            )
//        }
//        Box(
//            modifier = Modifier
//                .padding(horizontal = horizontalPadding)
//                .width(boxWidth)
//                .height(boxWidth)
//        ) {
//            CompassWindow(
//                modifier = Modifier.fillMaxSize(),
//                data = angleState.value
//            )
//        }
        Box(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .width(boxWidth)
                .height(boxWidth),
            contentAlignment = Alignment.Center
        ) {
            MotionWindow(
                modifier = Modifier.fillMaxSize(),
                data = imu9DataState.value
            )
        }
        VerticalSpacer(height = 40.dp)
        SciBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalPadding = horizontalPadding,
            backgroundColor = COLOR_BACKGROUND
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    SciText(text = "EMG", color = ColorSciBlue, fontSize = 20f)
                }
                EmgRtWindow(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .height(240.dp),
                        .height(100.dp),
                    emgDataState = emgDataState
                )
            }
        }
        VerticalSpacer(height = 20.dp)
        ZoomTime()
        VerticalSpacer(height = 20.dp)
        ZoomScale()
        VerticalSpacer(height = 20.dp)
        OptionItem(
            text = "50 HZ and 50 HZ Harmonic Filtering",
            onCheckedChange = {
                GlobalConfig.enableFiltering = it
            }
        )
        OptionItem(
            text = "EMG Recording",
            onCheckedChange = {
                if (it) {
                    DataManager.startRecordEmg()
                } else {
                    val path = DataManager.stopRecordEmg()
                    if (path != null) {
                        ToastManager.showToast(context, context.getString(R.string.key_store_data, "EMG", path))
                    } else {
                        ToastManager.showToast(context, context.getString(R.string.key_store_data_fail))
                    }
                }
            }
        )
        OptionItem(
            text = "IMU Recording",
            onCheckedChange = {
                if (it) {
                    DataManager.startRecordImu()
                } else {
                    val path = DataManager.stopRecordImu()
                    if (path != null) {
                        ToastManager.showToast(context, context.getString(R.string.key_store_data, "IMU", path))
                    } else {
                        ToastManager.showToast(context, context.getString(R.string.key_store_data_fail))
                    }
                }
            }
        )
    }
}


@Composable
fun BleFinderMenu(
    finder: MyoBleFinder?,
    onDeviceConnected: (MyoBleService) -> Unit,
    onDeviceDisconnected: () -> Unit
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
    val devices = remember { mutableListOf<Pair<String, BleDevice>>() }
    FinderMenu(
        value = selected.first,
        items = devices,
        onFinding = {
            if (finder?.isBluetoothEnabled() == false) {
                ToastManager.showToast(context, context.getString(R.string.key_enable_bluetooth))
                return@FinderMenu
            }
            CoroutineScope(Dispatchers.IO).launch {
                devices.clear()
                finder?.enableDebug(true)
                finder?.scan(object : MyoBleFinder.OnFinderUpdate {
                    override fun onStart() {
                        it.value = true
                    }

                    override fun onFound(peripheral: BleDevice) {
                        devices.removeIf { it.first == peripheral.mac }
                        devices.add(peripheral.mac to peripheral)
                    }

                    override fun onStop(peripherals: List<BleDevice>) {
                        if (it.value) {
                            it.value = false
                        }
                    }
                })
            }
        },
        onSelected = { loading, clicked, expanded, connectionState, it ->
            loading.value = false
            val delegate = BleDelegateDefaultImpl(it.second)
            val service = MyoBleService(delegate)
            CoroutineScope(Dispatchers.IO).launch {
                if (service.connect()) {
                    connectionState.value = true
                    selected = it
                    onDeviceConnected(service)
                } else {
                    connectionState.value = false
                }
                clicked.value = false
                expanded.value = false
            }
        },
        onUnselected = { connectionState ->
            onDeviceDisconnected()
            connectionState.value = false
            selected = Pair(context.getString(R.string.key_select_devices), null)
        },
        backgroundColor = ColorWhite
    )
}

@Composable
fun EmgRtWindow(
    modifier: Modifier,
    emgDataState: MutableState<List<Pair<Long, Float?>>>,
) {
//    RTWindow(
//        modifier = modifier,
//        data = emgDataState.value,
//        options = GlobalConfig.rtWindowOption
//    )
    Text(text = "\tEMG Record X3 [${emgDataState.value.firstOrNull()?.second}]", color = Color.White)
}

@Composable
fun GyroWindow(
    modifier: Modifier,
    data: Triple<Float, Float, Float>
) {
    Gyroscope(
        modifier = modifier,
        data = data,
        options = GlobalConfig.gyroscopeOption
    )
}

@Composable
fun CompassWindow(
    modifier: Modifier,
    data: Float
) {
    Compass(
        modifier = modifier,
        data = data,
        options = GlobalConfig.compassOption
    )
}

@Composable
fun MotionWindow(
    modifier: Modifier,
    data: List<Float>
) {
    Motion(
        modifier = modifier,
        data = data,
        options = GlobalConfig.motionOption
    )
}