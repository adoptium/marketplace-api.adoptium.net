package net.adoptium.marketplace.server.updater

import net.adoptium.marketplace.schema.ReleaseUpdateInfo
import net.adoptium.marketplace.schema.Vendor

interface Updater {
    suspend fun update(vendor: Vendor): ReleaseUpdateInfo
    fun scheduleUpdates()
}