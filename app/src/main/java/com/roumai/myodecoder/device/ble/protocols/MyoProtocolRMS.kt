package com.roumai.myodecoder.device.ble.protocols

object MyoProtocolRMS {
    private var startAt = -1L
    fun decode(stream: ByteArray): Pair<Long, IntArray>? {
        if (startAt == -1L) startAt = System.currentTimeMillis()
        var index = 0
        if (stream[index++] != 0x07.toByte()) return null
        var ts: Int = (stream[index++].toUByte().toInt() shl 24)
        ts = ts or (stream[index++].toUByte().toInt() shl 16)
        ts = ts or (stream[index++].toUByte().toInt() shl 8)
        ts = ts or (stream[index++].toUByte().toInt())
        val size = stream[index++].toUByte().toInt()
        val realRMSLength = (stream.size - 6) / 2
        if (size > realRMSLength)  return null
        val data = IntArray(size)
        var rms: Int
        for (i in 0 until size) {
            rms = stream[index++].toUByte().toInt() shl 8
            rms = rms.or(stream[index++].toUByte().toInt())
            data[i] = rms
        }
        return Pair(ts.toLong() + startAt, data)
    }
}