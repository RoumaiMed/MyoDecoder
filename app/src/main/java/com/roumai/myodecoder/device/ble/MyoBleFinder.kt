package com.roumai.myodecoder.device.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import java.util.UUID

/**
 * 靠 UUID 来区分蓝牙服务是否为 MyoDecoder
 */
private val SERVICE_UUID = UUID.fromString("0000ACE0-0000-1000-8000-00805f9b34fb")

class MyoBleFinder(autoConnect: Boolean) {
    private var debug = false

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
        val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner ?: return false
        val setting = ScanSettings.Builder().build()
        val filters = arrayListOf<ScanFilter>()
        callback.onStart()
        scanner
            .startScan(
                filters,
                setting,
                object : ScanCallback() {
                    private val devices = HashMap<String, BleDevice>()

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
                        callback.onStop(results?.mapNotNull { dev -> dev?.device?.let { BleDevice(it) } } ?: emptyList())
                    }

                    override fun onScanFailed(errorCode: Int) {
                    }
                })
        return true
    }


    interface OnFinderUpdate {
        fun onStart()

        fun onFound(peripheral: BleDevice)

        fun onStop(peripherals: List<BleDevice>)
    }
}