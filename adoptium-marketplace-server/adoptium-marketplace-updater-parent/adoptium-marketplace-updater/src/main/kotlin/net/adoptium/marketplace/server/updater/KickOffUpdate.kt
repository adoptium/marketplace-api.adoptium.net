package net.adoptium.marketplace.server.updater

import io.quarkus.arc.profile.UnlessBuildProfile
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@UnlessBuildProfile("test")
@ApplicationScoped
@Startup
class KickOffUpdate @Inject constructor(
    adoptiumMarketplaceUpdater: AdoptiumMarketplaceUpdater
) {
    init {
        adoptiumMarketplaceUpdater.scheduleUpdates()
    }
}
