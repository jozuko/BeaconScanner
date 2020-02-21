package com.studiojozu.beaconscanner.beacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import com.studiojozu.beaconscanner.event.AddLogEvent
import org.greenrobot.eventbus.EventBus

class BeaconManagerLollipop: BeaconManager {
    private val context: Context
    private val eventBus: EventBus = EventBus.getDefault()
    private val bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner: BluetoothLeScanner
    private val receivedUUID = mutableListOf<String>()

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(context: Context) {
        this.context = context
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            sendLog("onScanFailed")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val uuid = parseIBeaconUUID(result?.scanRecord?.bytes) ?: return
            if (receivedUUID.contains(uuid)) {
                return
            }

            sendLog("--------------------------")
            sendLog("onScanResult callbackType:$callbackType uuid: $uuid)")
            receivedUUID.add(uuid)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            sendLog("onBatchScanResults results: $results")
        }
    }

    private fun parseIBeaconUUID(data: ByteArray?): String? {
        data ?: return null
        if (data.count() < 27) return null

        val uuid = StringBuffer()
        uuid.append(String.format("%02x", data[6]))
        uuid.append(String.format("%02x", data[7]))
        uuid.append(String.format("%02x", data[8]))
        uuid.append(String.format("%02x", data[9]))
        uuid.append("-")
        uuid.append(String.format("%02x", data[10]))
        uuid.append(String.format("%02x", data[11]))
        uuid.append("-")
        uuid.append(String.format("%02x", data[12]))
        uuid.append(String.format("%02x", data[13]))
        uuid.append("-")
        uuid.append(String.format("%02x", data[14]))
        uuid.append(String.format("%02x", data[15]))
        uuid.append("-")
        uuid.append(String.format("%02x", data[16]))
        uuid.append(String.format("%02x", data[17]))
        uuid.append(String.format("%02x", data[18]))
        uuid.append(String.format("%02x", data[19]))
        uuid.append(String.format("%02x", data[20]))
        uuid.append(String.format("%02x", data[21]))

        return uuid.toString()
    }

    override fun onStart() {
        sendLog("----- 開始 -----")
        receivedUUID.clear()
        bluetoothLeScanner.startScan(scanCallback)
    }

    override fun onStop() {
        sendLog("----- 停止 -----")
        bluetoothLeScanner.stopScan(scanCallback)
        receivedUUID.clear()
    }

    private fun sendLog(message: String) {
        Log.d("BeaconManager", message)
        eventBus.post(AddLogEvent(message))
    }
}