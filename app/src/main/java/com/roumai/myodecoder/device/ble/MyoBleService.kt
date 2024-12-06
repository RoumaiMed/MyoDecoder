package com.roumai.myodecoder.device.ble

import android.os.ParcelUuid
import com.roumai.myodecoder.device.ble.protocols.MyoProtocolEMG
import com.roumai.myodecoder.device.ble.protocols.MyoProtocolIMU
import com.roumai.myodecoder.device.ble.protocols.MyoProtocolRMS

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
        var cnt = 0
        var startAt = -1L
        return delegate.subscribe(KEY_EMG) {
            if (cnt++ < 5) return@subscribe // skip first 5 packet #TODO
            // emg protocol: decode here
            val emg = MyoProtocolEMG.decode(stream = it, channelSize = 3) ?: return@subscribe
            if (startAt == -1L) startAt = System.currentTimeMillis() - emg[0].first
            val result = emg.map { data -> Pair(data.first + startAt, data.second) }
            callback(result)
        }
    }

    suspend fun stopObserveEMG(): Boolean {
        return delegate.unsubscribe(KEY_EMG)
    }

    suspend fun observeIMU(callback: (Pair<Long, FloatArray>) -> Unit): Boolean {
        var cnt = 0
        var startAt = -1L
        return delegate.subscribe(KEY_IMU) {
            if (cnt++ < 5) return@subscribe // skip first 5 packet #TODO
            // imu protocol: decode here
            val imu = MyoProtocolIMU.decode(stream = it) ?: return@subscribe
            if (startAt == -1L) startAt = System.currentTimeMillis() - imu.first
            val result = Pair(imu.first + startAt, imu.second)
            callback(result)
        }
    }

    suspend fun stopObserveIMU(): Boolean {
        return delegate.unsubscribe(KEY_IMU)
    }

    suspend fun observeRMS(callback: (Pair<Long, IntArray>) -> Unit): Boolean {
        var cnt = 0
        var startAt = -1L
        return delegate.subscribe(KEY_RMS) {
            if (cnt++ < 5) return@subscribe // skip first 5 packet #TODO
            // rms protocol: decode here
            val rms = MyoProtocolRMS.decode(stream = it) ?: return@subscribe
            if (startAt == -1L) startAt = System.currentTimeMillis() - rms.first
            val result = Pair(rms.first + startAt, rms.second)
            callback(result)
        }
    }

    suspend fun stopObserveRMS(): Boolean {
        return delegate.unsubscribe(KEY_RMS)
    }
}