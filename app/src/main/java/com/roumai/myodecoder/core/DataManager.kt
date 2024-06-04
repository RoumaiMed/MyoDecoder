package com.roumai.myodecoder.core

import androidx.compose.runtime.mutableStateOf
import com.roumai.myodecoder.device.ble.MyoBleService
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object DataManager {
    private val service = mutableStateOf<MyoBleService?>(null)
    private val emgData = ConcurrentHashMap<Long, Pair<Long, IntArray>>()
    private val gyro = mutableStateOf(Triple(0f, 0f, 0f))

    fun startService(
        s: MyoBleService,
        onEmgCallback: (List<Pair<Long, Float?>>) -> Unit,
        onGyroCallback: (Triple<Float, Float, Float>) -> Unit
    ) {
        service.value = s
        CoroutineScope(Dispatchers.IO).launch {
            service.value!!.observeEMG { dataList ->
                dataList.forEach { data ->
                    addEmg(data.first, data.second)
                }
            }
            service.value!!.observeIMU { data ->
                val x = data.second[4]
                val y = data.second[5]
                val z = data.second[6]
                updateGyro(x, y, z)
            }
            service.value!!.observeRMS {

            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            while (service.value != null) {
                val emgData = getEmg()
                onEmgCallback(emgData)
                val gyroData = getGyro()
                onGyroCallback(gyroData.value)
                delay(10L)
            }
        }
    }

    fun removeService() {
        CoroutineScope(Dispatchers.IO).launch {
            service.value?.disconnect()
            service.value = null
        }
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
                result.add(Pair(rawSignal[i].first, if (idxArr[i]) null else filteredSignal[i].toFloat()))
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

}