package com.roumai.myodecoder.device.ble

import android.os.ParcelUuid
import com.roumai.myodecoder.device.ble.protocols.MyoProtocolEMG
import com.roumai.myodecoder.device.ble.protocols.MyoProtocolIMU

private val KEY_EMG = BleDelegateKey(
    service = ParcelUuid.fromString("0000ACE0-0000-1000-8000-00805f9b34fb"),
    characteristic = ParcelUuid.fromString("0000ACE3-0000-1000-8000-00805f9b34fb"),
)
private val KEY_RMS = BleDelegateKey(
    service = ParcelUuid.fromString("0000ACE0-0000-1000-8000-00805f9b34fb"),
    characteristic = ParcelUuid.fromString("0000ACE4-0000-1000-8000-00805f9b34fb"),
)
private val KEY_IMU = BleDelegateKey(
    service = ParcelUuid.fromString("0000ACE0-0000-1000-8000-00805f9b34fb"),
    characteristic = ParcelUuid.fromString("0000ACE2-0000-1000-8000-00805f9b34fb"),
)


class MyoBleService(override val delegate: BleDelegate) : CommonBleService(delegate = delegate) {

    suspend fun observeEMG(callback: (List<Pair<Long, IntArray>>) -> Unit): Boolean {
        return delegate.subscribe(KEY_EMG) {
            // emg protocol: decode here
            val emg = MyoProtocolEMG.decode(stream = it, channelSize = 16) ?: return@subscribe
            callback(emg)
        }
    }

    suspend fun observeIMU(callback: (Pair<Long, FloatArray>) -> Unit): Boolean {
        return delegate.subscribe(KEY_IMU) {
            // imu protocol: decode here
            val imu = MyoProtocolIMU.decode(stream = it) ?: return@subscribe
            callback(imu)
        }
    }
}