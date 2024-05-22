package com.roumai.myodecoder.device.ble.impl

import android.bluetooth.BluetoothGatt
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.roumai.myodecoder.device.ble.BleDelegate
import com.roumai.myodecoder.device.ble.BleDelegateKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleDelegateDefaultImpl(private val device: BleDevice) : BleDelegate {
    override val name = device.name ?: "(Unknown)"
    override val mac = device.mac ?: ""
    private var connected = false

    override suspend fun connect(): Boolean = suspendCoroutine { cor ->
        BleManager.getInstance().connect(device, object : BleGattCallback() {
            override fun onStartConnect() {
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                connected = false
                if (cor.context.isActive) cor.resume(false)
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                connected = true
                if (cor.context.isActive) cor.resume(true)
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                connected = false
                if (cor.context.isActive) cor.resume(false)
            }
        })
    }

    override suspend fun isConnected(): Boolean = connected

    override suspend fun disconnect(): Boolean {
        BleManager.getInstance().disconnect(device)
        return true
    }

    override suspend fun read(key: BleDelegateKey): ByteArray? = suspendCoroutine {
        BleManager.getInstance().read(
            device,
            key.service.toString(),
            key.characteristic.toString(),
            object : BleReadCallback() {

                @Synchronized
                override fun onReadSuccess(data: ByteArray?) {
                    if (it.context.isActive) it.resume(data)
                }

                @Synchronized
                override fun onReadFailure(exception: BleException?) {
                    if (it.context.isActive) it.resume(byteArrayOf())
                }
            })
    }

    override suspend fun write(key: BleDelegateKey, data: ByteArray): Boolean = suspendCoroutine {
        BleManager.getInstance().write(
            device,
            key.service.toString(),
            key.characteristic.toString(),
            data,
            object : BleWriteCallback() {

                @Synchronized
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    if (it.context.isActive) it.resume(true)
                }

                @Synchronized
                override fun onWriteFailure(exception: BleException?) {
                    if (it.context.isActive) it.resume(false)
                }
            })
    }

    override suspend fun subscribe(key: BleDelegateKey, callback: (ByteArray) -> Unit): Boolean {
        delay(30)
        return suspendCoroutine {
            BleManager.getInstance().notify(
                device,
                key.service.toString(),
                key.characteristic.toString(),
                object : BleNotifyCallback() {

                    @Synchronized
                    override fun onNotifySuccess() {
                        if (it.context.isActive) it.resume(true)
                    }

                    @Synchronized
                    override fun onNotifyFailure(exception: BleException?) {
                        if (it.context.isActive) it.resume(false)
                    }

                    override fun onCharacteristicChanged(data: ByteArray?) {
                        if (data != null) {
                            callback(data)
                        }
                    }
                })
        }
    }

    override suspend fun unsubscribe(key: BleDelegateKey): Boolean = BleManager.getInstance()
        .stopNotify(device, key.service.toString(), key.characteristic.toString())

    override suspend fun rssi(): Int = suspendCoroutine {
        BleManager.getInstance().readRssi(device, object : BleRssiCallback() {
            override fun onRssiFailure(exception: BleException?) {
                if (it.context.isActive) it.resume(0)
            }

            override fun onRssiSuccess(rssi: Int) {
                if (it.context.isActive) it.resume(rssi)
            }
        })
    }


    override suspend fun mtu(mtu: Int): Boolean = suspendCoroutine {
        BleManager.getInstance().setMtu(device, mtu, object : BleMtuChangedCallback() {
            override fun onMtuChanged(mtu: Int) {
                if (it.context.isActive) it.resume(true)
            }

            override fun onSetMTUFailure(exception: BleException?) {
                if (it.context.isActive) it.resume(false)
            }
        })
    }
}
