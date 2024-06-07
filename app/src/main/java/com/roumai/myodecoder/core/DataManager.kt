package com.roumai.myodecoder.core

import androidx.compose.runtime.mutableStateOf
import com.roumai.myodecoder.device.ble.MyoBleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.lang.Math.toDegrees
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.math.atan2

object DataManager {
    private val service = mutableStateOf<MyoBleService?>(null)
    private val emgData = ConcurrentHashMap<Long, Pair<Long, IntArray>>()
    private val gyro = mutableStateOf(Triple(0f, 0f, 0f))
    private val angle = mutableStateOf(90f)
    private var recordingDir = ""
    private val recordEmg = mutableStateOf(false)
    private var emgCsvFile: CSV? = null
    private val recordImu = mutableStateOf(false)
    private var imuCsvFile: CSV? = null

    fun setRecordingDir(dir: String) {
        recordingDir = dir
    }

    fun getRecordingDir() = recordingDir

    private var serviceJob: Job? = null
    private var uiJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun startService(
        service: MyoBleService,
        onEmgCallback: (List<Pair<Long, Float?>>) -> Unit,
        onGyroCallback: (Triple<Float, Float, Float>) -> Unit,
        onAngleCallback: (Float) -> Unit
    ) {
        DataManager.service.value = service
        serviceJob?.cancel()
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            DataManager.service.value?.let {
                it.observeEMG { dataList ->
                    dataList.forEach { data ->
                        addEmg(data.first, data.second)
                        if (recordEmg.value) {
                            emgCsvFile?.append(data.first, data.second)
                        }
                    }
                }
                it.observeIMU { data ->
                    if (recordImu.value) {
                        imuCsvFile?.append(data.first, data.second)
                    }
                    val gx = data.second[4]
                    val gy = data.second[5]
                    val gz = data.second[6]
                    updateGyro(gx, gy, gz)
                    val mx = data.second[7]
                    val my = data.second[8]
                    val mz = data.second[9]
                    updateAngle(mx, my, mz)
                }
                it.observeRMS {

                }
            }
        }
        uiJob?.cancel()
        uiJob = CoroutineScope(newSingleThreadContext("DataProcess")).launch {
            while (isActive && DataManager.service.value != null) {
//                val emgData = getEmg()
                val currentAt = System.currentTimeMillis()
                val emgData = fetchEMG(emgData, (currentAt - GlobalConfig.windowSize) until currentAt, 1)
                val gyroData = getGyro()
                val angleData = getAngle()
                withContext(Dispatchers.Main) {
                    onEmgCallback(emgData)
                    onGyroCallback(gyroData.value)
                    onAngleCallback(angleData.value)
                }
                delay(10L)
            }
        }
    }

    fun removeService() {
        serviceJob?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            service.value?.disconnect()
            service.value = null
        }
    }

    fun startRecordEmg() {
        recordEmg.value = true
        emgCsvFile = CSV(CSVType.EMG, GlobalConfig.CHANNEL_NUM)
    }

    fun stopRecordEmg(): String? {
        recordEmg.value = false
        emgCsvFile?.close()
        val path = emgCsvFile?.getPath()
        emgCsvFile = null
        return path
    }

    fun startRecordImu() {
        recordImu.value = true
        imuCsvFile = CSV(CSVType.IMU, 9)
    }

    fun stopRecordImu(): String? {
        recordImu.value = false
        imuCsvFile?.close()
        val path = imuCsvFile?.getPath()
        imuCsvFile = null
        return path
    }

    fun addEmg(timestamp: Long, data: IntArray) {
        emgData[timestamp] = Pair(timestamp, data)
        if (emgData.size > GlobalConfig.DATA_STORE_SIZE) {
            emgData.keys
                .toList()
                .take(emgData.size - GlobalConfig.DATA_STORE_SIZE)
                .forEach { emgData.remove(it) }
        }
    }

    private fun fetchEMG(
        data: ConcurrentHashMap<Long, Pair<Long, IntArray>>,
        range: LongRange,
        minInterval: Long, // in ms
    ): MutableList<Pair<Long, Float?>> {
        val result = mutableListOf<Pair<Long, Float?>>()

        for (t in range step minInterval) {
            val volt = data[t]?.second?.first()?.let {
                (it - 8192) / 8192.0f * 1.65f
            }
            result.add(Pair(t, volt))
        }

        if (GlobalConfig.enableFiltering) {
            var filteredSignal = result.map { it.second?.toDouble() ?: 0.0 }.toDoubleArray()
            val frequency = 50.0
            (1..9).map { Pair(it * frequency - 2, it * frequency + 2) }.forEach {
                filteredSignal = SignalProcessor.filter(
                    data = filteredSignal,
                    samplingRate = GlobalConfig.SAMPLE_RATE.toDouble(),
                    frequencyInterval = it,
                )
            }
            // update result value if value is not null
            for (i in result.indices) {
                if (result[i].second == null) continue
                result[i] = Pair(result[i].first, filteredSignal[i].toFloat())
            }
        }

        return result
    }

    fun getEmg(): MutableList<Pair<Long, Float?>> {
        val currentAt = System.currentTimeMillis()
        val lastAt = currentAt - GlobalConfig.windowSize
        val rawSignal = mutableListOf<Pair<Long, Float>>()
        val idxArr = BooleanArray((currentAt - lastAt).toInt()) { false }
        for (ts in lastAt until currentAt) {
            val data = emgData[ts]?.second?.first()
            if (data == null) {
                rawSignal.add(Pair(ts, 0f))
                idxArr[(ts - lastAt).toInt()] = true
                continue
            }
            val volt = (data - 8192) / 8192.0f * 1.65f
            rawSignal.add(Pair(ts, volt))
        }
        val result = mutableListOf<Pair<Long, Float?>>()
        if (GlobalConfig.enableFiltering) {
            var filteredSignal = SignalProcessor.filter(
                rawSignal.map { it.second.toDouble() }.toDoubleArray(),
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(48.0, 52.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(98.0, 102.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(148.0, 152.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(198.0, 202.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(248.0, 252.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(298.0, 302.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(348.0, 352.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(398.0, 402.0)
            )
            filteredSignal = SignalProcessor.filter(
                filteredSignal,
                GlobalConfig.SAMPLE_RATE.toDouble(),
                Pair(448.0, 452.0)
            )
            for (i in idxArr.indices) {
                result.add(
                    Pair(
                        rawSignal[i].first,
                        if (idxArr[i]) null else filteredSignal[i].toFloat()
                    )
                )
            }
        } else {
            for (i in idxArr.indices) {
                result.add(Pair(rawSignal[i].first, if (idxArr[i]) null else rawSignal[i].second))
            }
        }
        return result
    }

    fun updateGyro(x: Float, y: Float, z: Float) {
        gyro.value = Triple(x, y, z)
    }

    fun getGyro() = gyro

    fun updateAngle(mx: Float, my: Float, mz: Float) {
        var heading = atan2(my, mx)
        heading = toDegrees(heading.toDouble()).toFloat()
        if (heading < 0) {
            heading += 360f
        }
        angle.value = heading
    }

    fun getAngle() = angle
}