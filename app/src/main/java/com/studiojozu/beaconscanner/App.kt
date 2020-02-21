package com.studiojozu.beaconscanner

import android.app.Application
import com.studiojozu.beaconscanner.infrastructure.realm.RealmManager
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by r.mori on 2020-02-21.
 * Copyright (c) 2020 rei-frontier. All rights reserved.
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
    }

    private fun setUpRealm() {
        RealmManager.setUp(this)
    }
}