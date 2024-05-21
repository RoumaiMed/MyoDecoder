package com.roumai.myodecoder.device.ble.impl

import com.roumai.myodecoder.device.ble.BleDelegate
import com.roumai.myodecoder.device.ble.BleDelegateKey

class BleDelegateDefaultImpl: BleDelegate {
    override suspend fun connect(autoReconnect: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isConnected(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun read(key: BleDelegateKey): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun write(key: BleDelegateKey, data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun subscribe(key: BleDelegateKey, callback: (ByteArray) -> Unit): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun unsubscribe(key: BleDelegateKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun rssi(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun mtu(mtu: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deviceName(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun deviceMacAddress(): String? {
        TODO("Not yet implemented")
    }
}