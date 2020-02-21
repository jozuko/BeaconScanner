package com.studiojozu.beaconscanner.beacon

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class BeaconManagerFactory {
    companion object {
        private var lock = Lock()

        @SuppressLint("StaticFieldLeak")
        private var instance: BeaconManager? = null

        fun getInstance(context: Context): BeaconManager {
            synchronized(lock) {
                if (instance == null) {
                    instance = if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        // BLEが利用出来ない端末だった場合の処理を記述
                        BeaconManagerUnsupported()
                    } else {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> BeaconManagerLollipop(context)
                            else -> BeaconManagerJellyBeansMr2(context)
                        }
                    }
                }
            }
            return instance!!
        }
    }

    private class Lock

}