package com.roumai.myodecoder.device.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 靠 UUID 来区分蓝牙服务是否为 MyoDecoder
 */
private val SERVICE_UUID = UUID.fromString("0000ACE0-0000-1000-8000-00805f9b34fb")

class MyoBleFinder(autoConnect: Boolean) {
    private var debug = false
    private val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private var callback: OnFinderUpdate? = null
    private val devices = HashMap<String, BleDevice>()

    companion object private

    val TIMEOUT = 12000

    init {
        BleManager.getInstance()
            .initScanRule(
                BleScanRuleConfig.Builder()
                    .setServiceUuids(arrayOf(SERVICE_UUID))
                    .setAutoConnect(autoConnect)
                    .build()
            )
    }

    fun enableDebug(debug: Boolean) {
        this.debug = debug
    }

    @SuppressLint("MissingPermission")
    fun scan(callback: OnFinderUpdate): Boolean {
        this.callback = callback
        if (scanner == null) return false

        // timeout
        val job = CoroutineScope(Dispatchers.IO).launch {
            var timeout = TIMEOUT
            while (isActive && timeout > 0) {
                timeout -= 1000
                delay(1000)
            }
            if (timeout <= 0) {
                scanner.stopScan(object : ScanCallback() {})
                callback.onStop(devices.mapNotNull { it.value })
            }
        }

        // start scan
        val setting = ScanSettings.Builder().build()
        val filters = arrayListOf<ScanFilter>(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )
        devices.clear()
        callback.onStart()
        scanner
            .startScan(
                filters,
                setting,
                object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult?) {
                        val device = result?.device ?: return

                        val ble = BleDevice(device)
                        // 去重 & filter
                        val dev = devices[device.address]
                        if (dev == null) {
                            devices[device.address] = ble
                            if (debug) println("Scan Found: ${device.name} | ${device.address}")
                            callback.onFound(ble)
                        }
                    }

                    override fun onBatchScanResults(results: List<ScanResult?>?) {
                        job.cancel()
                        callback.onStop(devices.mapNotNull { it.value })
                    }

                    override fun onScanFailed(errorCode: Int) {
                    }
                })

        return true
    }

    @SuppressLint("MissingPermission")
    fun stop(): Boolean {
        scanner?.stopScan(object : ScanCallback() {})
        this.callback?.onStop(devices.mapNotNull { it.value })
        this.callback = null
        return true
    }

    interface OnFinderUpdate {
        fun onStart()

        fun onFound(peripheral: BleDevice)

        fun onStop(peripherals: List<BleDevice>)
    }
}