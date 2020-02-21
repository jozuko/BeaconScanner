package com.studiojozu.beaconscanner.infrastructure.realm

import io.realm.DynamicRealm
import io.realm.RealmMigration

/**
 * Created by r.mori on 2020-02-21.
 * Copyright (c) 2020 rei-frontier. All rights reserved.
 */
class RealmMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        // no migrate
    }
}