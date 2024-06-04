package com.roumai.myodecoder.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.roumai.myodecoder.device.ble.MyoBleFinder
import com.roumai.myodecoder.device.ble.MyoBleService
import com.roumai.myodecoder.device.ble.impl.BleDelegateDefaultImpl
import com.roumai.myodecoder.ui.components.*
import com.roumai.myodecoder.ui.theme.ColorSciBlue
import com.roumai.myodecoder.ui.utils.ToastManager
import kotlinx.coroutines.*


@Composable
fun Main(
    finder: MyoBleFinder?
) {
    val emgDataState = remember { mutableStateOf<List<Pair<Long, Float?>>>(emptyList()) }
    val gyroDataState = remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (isActive) {
                if (!DataManager.isActive) {
                    delay(1000L)
                    continue
                }
                val emgData = DataManager.getEmg()
                emgDataState.value = emgData
                val gyroData = DataManager.getGyro()
                gyroDataState.value = gyroData.value
                delay(10L)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF231815))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val config = LocalConfiguration.current
        val horizontalPadding = 12.dp
        VerticalSpacer(height = 20.dp)
        BleFinderMenu(
            finder = finder,
            onDeviceConnected = {
                DataManager.isActive = true
                CoroutineScope(Dispatchers.IO).launch {
                    it.observeEMG { dataList ->
                        dataList.forEach { data ->
                            DataManager.addEmg(data.first, data.second)
                        }
                    }
                    it.observeIMU { data ->
                        val x = data.second[4]
                        val y = data.second[5]
                        val z = data.second[6]
                        DataManager.updateGyro(x, y, z)
                    }
                    it.observeRMS {

                    }
                }
            }
        )
        VerticalSpacer(height = 40.dp)
        val boxWidth = config.screenWidthDp.dp - horizontalPadding * 2
        Box(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .width(boxWidth)
                .height(boxWidth)
        ) {
            GyroWindow(
                modifier = Modifier
                    .fillMaxSize(),
                data = gyroDataState.value,
            )
        }
        VerticalSpacer(height = 40.dp)
        SciBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalPadding = horizontalPadding,
            backgroundColor = Color(0xFF231815)
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
                        .height(240.dp),
                    emgDataState = emgDataState
                )
            }
        }
    }
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

@Composable
fun EmgRtWindow(
    modifier: Modifier,
    emgDataState: MutableState<List<Pair<Long, Float?>>>,
) {
    val options = remember { RTWindowOption() }
    RTWindow(
        modifier = modifier,
        data = emgDataState.value,
        options = options
    )
}

@Composable
fun GyroWindow(
    modifier: Modifier,
    data: Triple<Float, Float, Float>
) {
    val options = remember {
        GyroscopeOption(
            Color(0xFF231815),
            Color.White
        )
    }
    Gyroscope(
        modifier = modifier,
        data = data,
        options = options
    )
}