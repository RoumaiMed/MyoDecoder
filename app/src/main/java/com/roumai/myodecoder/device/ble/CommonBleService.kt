package com.roumai.myodecoder.device.ble

import android.os.ParcelUuid

private val KEY_BATTERY = BleDelegateKey(
    service = ParcelUuid.fromString("0000180f-0000-1000-8000-00805f9b34fb"),
    characteristic = ParcelUuid.fromString("00002a19-0000-1000-8000-00805f9b34fb"),
)

open class CommonBleService(open val delegate: BleDelegate) {
    suspend fun connect(): Boolean {
        val success = delegate.connect(true)
        if (success) {
            // update mtu.
            return delegate.mtu(502)
        }
        return false
    }

    suspend fun isConnect() = delegate.isConnected()

    suspend fun disconnect() = delegate.disconnect()

    suspend fun observeBatteryLevel(callback: (Int) -> Unit): Boolean {
        val decode: (ByteArray?) -> Int? = {
            if (it == null) null
            else if (it.isNotEmpty()) {
                it[0].toInt()
            } else null
        }
        // read once
        val data = delegate.read(KEY_BATTERY)
        decode(data)?.let(callback)

        // subscribe
        return delegate.subscribe(KEY_BATTERY) {
            decode(it)?.let(callback)
        }
    }
}