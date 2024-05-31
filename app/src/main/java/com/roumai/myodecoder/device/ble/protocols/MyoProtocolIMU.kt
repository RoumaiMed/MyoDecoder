package com.roumai.myodecoder.device.ble.protocols

object MyoProtocolIMU {
    fun decode(stream: ByteArray): Pair<Long, FloatArray>? {
        var index = 0
        if (stream[index++] != 0x04.toByte()) return null
        var ts: Int = (stream[index++].toUByte().toInt() shl 24)
        ts = ts or (stream[index++].toUByte().toInt() shl 16)
        ts = ts or (stream[index++].toUByte().toInt() shl 8)
        ts = ts or (stream[index++].toUByte().toInt())
        val size = 10
        val data = FloatArray(size)
        var value: Int
        for (i in 0 until size) {
            value = stream[index++].toUByte().toInt() shl 24
            value = value.or(stream[index++].toUByte().toInt() shl 16)
            value = value.or(stream[index++].toUByte().toInt() shl 8)
            value = value.or(stream[index++].toUByte().toInt())
            data[i] = value / 1000.0f
        }
        return Pair(ts.toLong(), data)
    }
}