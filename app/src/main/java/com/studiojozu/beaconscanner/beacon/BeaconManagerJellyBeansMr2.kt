package com.studiojozu.beaconscanner.beacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.studiojozu.beaconscanner.event.AddLogEvent
import org.greenrobot.eventbus.EventBus

class BeaconManagerJellyBeansMr2: BeaconManager {
    private val context: Context
    private val eventBus: EventBus = EventBus.getDefault()
    private val bluetoothAdapter: BluetoothAdapter
    private val receivedUUID = mutableListOf<String>()

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(context: Context) {
        this.context = context
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private val scanCallback = BluetoothAdapter.LeScanCallback { _, _, scanRecord ->
        val uuid = parseIBeaconUUID(scanRecord) ?: return@LeScanCallback
        if(receivedUUID.contains(uuid)){
            return@LeScanCallback
        }
        sendLog("--------------------------")
        sendLog("onScanResult uuid: $uuid)")
        receivedUUID.add(uuid)
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
        @Suppress("DEPRECATION")
        bluetoothAdapter.startLeScan(scanCallback)
    }

    override fun onStop() {
        sendLog("----- 停止 -----")
        @Suppress("DEPRECATION")
        bluetoothAdapter.stopLeScan(scanCallback)
        receivedUUID.clear()
    }

    private fun sendLog(message: String) {
        Log.d("BeaconManager", message)
        eventBus.post(AddLogEvent(message))
    }
}