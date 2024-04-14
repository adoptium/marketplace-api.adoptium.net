package net.adoptium.marketplace.server.frontend.routes

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import kotlinx.coroutines.runBlocking
import net.adoptium.marketplace.dataSources.APIDataStore
import net.adoptium.marketplace.schema.ReleaseList
import net.adoptium.marketplace.schema.Vendor
import net.adoptium.marketplace.server.frontend.OpenApiDocs
import net.adoptium.marketplace.server.frontend.models.ReleaseUpdateSummary
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter

@Path("/v1/updateStatus")
@ApplicationScoped
class VendorStatusRoute
@Inject
constructor(private val apiDataStore: APIDataStore) {
    @GET
    @Schema(hidden = true)
    @Path("/forVendor/{vendor}")
    @Operation(hidden = true)
    fun getVendorUpdateStatus(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor
    ): List<ReleaseUpdateSummary> {
        return runBlocking {
            return@runBlocking apiDataStore
                .getReleases(vendor)
                .getReleaseVendorStatus()
                .map {
                    ReleaseUpdateSummary(
                        toSummary(it.added),
                        toSummary(it.updated),
                        toSummary(it.removed),
                        it.timestamp
                    )
                }
        }
    }

    private fun toSummary(list: ReleaseList): List<String> {
        return list.releases.map { it.releaseName }
    }
}
