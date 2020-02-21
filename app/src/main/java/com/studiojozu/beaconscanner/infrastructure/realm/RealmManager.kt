package com.studiojozu.beaconscanner.infrastructure.realm

import android.app.Application
import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by r.mori on 2020-02-21.
 * Copyright (c) 2020 rei-frontier. All rights reserved.
 */
object RealmManager {
    fun setUp(application: Application) {
        init(application)
        setUpConfiguration()
    }

    private fun init(application: Application) {
        Realm.init(application)
    }

    private fun setUpConfiguration() {
        RealmConfiguration.Builder()
            .name("beacon-scanner.realm")
            .schemaVersion(1)
            .migration(RealmMigration())
            .build()
    }
}