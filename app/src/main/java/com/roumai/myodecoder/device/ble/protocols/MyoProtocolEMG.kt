package com.roumai.myodecoder.device.ble.protocols

object MyoProtocolEMG {
    fun decode(stream: ByteArray, channelSize: Int = 16): List<Pair<Long, IntArray>>? {
        var index = 0
        if (stream[index++] != 0x03.toByte()) return null
        var size: UInt
        val result = ArrayList<Pair<Long, IntArray>>()
        while (index < stream.size) {
            var ts = stream[index++].toUByte().toUInt() shl 24
            ts = ts or (stream[index++].toUByte().toUInt() shl 16)
            ts = ts or (stream[index++].toUByte().toUInt() shl 8)
            ts = ts or (stream[index++].toUByte().toUInt())
            size = stream[index++].toUByte().toUInt()
            if (size > 128u) {
                break
            }
            val dataA = IntArray(channelSize)
            for (i in 0 until size.toInt()) {
                var volt = stream[index++].toUByte().toInt() shl 8
                volt = volt or (stream[index++].toUByte().toInt())
                dataA[i] = volt
            }
            for (i in size.toInt() until channelSize) {
                dataA[i] = 0
            }
            result.add(Pair(ts.toLong(), dataA))
        }
        return result
    }
}