package net.adoptium.marketplace.server.frontend

import io.quarkus.runtime.Quarkus

import io.quarkus.runtime.annotations.QuarkusMain
import net.adoptium.api.marketplace.ai.AppInsightsTelemetry

@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // force eager startup of AppInsights, must be done from the main thread
        AppInsightsTelemetry.enabled
        Quarkus.run(*args)
    }
}
