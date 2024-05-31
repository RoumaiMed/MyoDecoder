package com.roumai.myodecoder.core

import java.util.concurrent.ConcurrentHashMap

object DataManager {
    var isActive = false
    private val emgData = ConcurrentHashMap<Long, Pair<Long, IntArray>>()

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
            val data = emgData[ts]?.second?.get(0)?.toFloat()?.div(1000f)
            result.add(Pair(ts, data))
        }
        return result
    }
}