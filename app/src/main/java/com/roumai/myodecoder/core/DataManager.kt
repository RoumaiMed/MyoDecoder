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
        val result = mutableListOf<Pair<Long, Float?>>()
        for (ts in lastAt until currentAt) {
            val data = emgData[ts]?.second?.first()
            if (data == null) {
                result.add(Pair(ts, null))
                continue
            }
            val volt = (data - 8192) / 8192.0f * 1.65f
            result.add(Pair(ts, volt))
        }
        return result
    }

    fun updateGyro(x: Float, y: Float, z: Float) {
        gyro.value = Triple(x, y, z)
    }

    fun getGyro() = gyro

}