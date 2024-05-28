package com.roumai.myodecoder.device.ble.protocols

object MyoProtocolEMG {
    fun decode(data: ByteArray, channelSize: Int = 16): List<Pair<Long, IntArray>>? {
        if (data[0] == 0x03.toByte()) {
            var index = 1
            var timestamp: Long
            var timestamp_pre: UInt
            var size: UInt
            val result = ArrayList<Pair<Long, IntArray>>()
            while (index < data.size) {
                timestamp_pre = data[index++].toUByte().toUInt() shl 24
                timestamp_pre = timestamp_pre.or(data[index++].toUByte().toUInt() shl 16)
                timestamp_pre = timestamp_pre.or(data[index++].toUByte().toUInt() shl 8)
                timestamp_pre = timestamp_pre.or(data[index++].toUByte().toUInt())
                timestamp = (timestamp_pre.toLong() * 1000)
                size = data[index++].toUByte().toUInt()
                if (size > 128u) { // not a valid packet!
                    break
                }
                val dataA = IntArray(channelSize)
                var volt = 0
                for (i in 0 until size.toInt()) {
                    volt = data[index++].toUByte().toInt() shl 8
                    volt = volt.or(data[index++].toUByte().toInt())
                    dataA[i] = volt
                }
                for (i in size.toInt() until channelSize) {
                    dataA[i] = 0
                }
                result.add(Pair(timestamp, dataA))
            }
            return result
        }
        return null
    }
}