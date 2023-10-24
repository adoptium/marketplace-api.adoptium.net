package net.adoptium.marketplace.server.frontend.routes

import net.adoptium.marketplace.dataSources.APIDataStore
import net.adoptium.marketplace.schema.ReleaseList
import net.adoptium.marketplace.schema.Vendor
import net.adoptium.marketplace.server.frontend.OpenApiDocs
import net.adoptium.marketplace.server.frontend.models.ReleaseUpdateSummary
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import jakarta.inject.Inject
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam

@Path("/v1/updateStatus")
@Schema(hidden = true)
@ApplicationScoped
class VendorStatusRoute
@Inject
constructor(private val apiDataStore: APIDataStore) {
    @GET
    @Schema(hidden = true)
    @Path("/forVendor/{vendor}")
    @Operation(hidden = true)
    suspend fun getVendorUpdateStatus(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor
    ): List<ReleaseUpdateSummary> {
        return apiDataStore
            .getReleases(vendor)
            .getReleaseVendorStatus()
            .map {
                ReleaseUpdateSummary(toSummary(it.added), toSummary(it.updated), toSummary(it.removed), it.timestamp)
            }
    }

    private fun toSummary(list: ReleaseList): List<String> {
        return list.releases.map { it.releaseName }
    }
}
