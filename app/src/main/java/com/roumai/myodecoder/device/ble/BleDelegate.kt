package com.roumai.myodecoder.device.ble

import android.os.ParcelUuid

interface BleDelegate {
    suspend fun connect(autoReconnect: Boolean): Boolean

    suspend fun isConnected(): Boolean

    suspend fun disconnect(): Boolean

    suspend fun read(key: BleDelegateKey): ByteArray?

    suspend fun write(key: BleDelegateKey, data: ByteArray): Boolean

    suspend fun subscribe(key: BleDelegateKey, callback: (ByteArray) -> Unit): Boolean

    suspend fun unsubscribe(key: BleDelegateKey): Boolean

    suspend fun rssi(): Int

    suspend fun mtu(mtu: Int): Boolean

    suspend fun deviceName(): String?

    suspend fun deviceMacAddress(): String?
}

data class BleDelegateKey(
    val service: ParcelUuid,
    val characteristic: ParcelUuid,
)
