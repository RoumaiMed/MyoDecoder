package com.roumai.myodecoder.core

import java.io.BufferedWriter
import java.nio.file.Paths

class CSV(
    csvType: CSVType,
    num: Int
) {
    private val writer: BufferedWriter
    private val path: String

    init {
        val filename = DataManager.getRecordingDir() + "/${csvType.type}-${TimeConvertor.getTime()}.csv"
        val file = Paths.get(filename).toFile()
        writer = BufferedWriter(file.writer())
        path = file.absolutePath

        when (csvType) {
            CSVType.EMG -> {
                writer.write("timestamp," + (1..num).joinToString(",") + "\n")
            }

            CSVType.IMU -> {
                writer.write("timestamp,ax,ay,az,gx,gy,gz,mx,my,mz\n")
            }
        }
    }

    fun getPath(): String = path

    fun append(timestamp: Long, data: FloatArray) {
        writer.write("$timestamp," + data.joinToString(",") + "\n")
    }

    fun append(timestamp: Long, data: IntArray) {
        writer.write("$timestamp," + data.joinToString(",") + "\n")
    }

    fun close() {
        writer.close()
    }
}

enum class CSVType(val type: String) {
    EMG("emg"),
    IMU("imu")
}