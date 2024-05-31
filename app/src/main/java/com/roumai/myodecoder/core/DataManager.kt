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
}