package net.adoptium.marketplace.server.frontend.routes.info

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.runBlocking
import net.adoptium.marketplace.dataSources.APIDataStore
import net.adoptium.marketplace.dataSources.ReleaseInfo
import net.adoptium.marketplace.schema.Vendor
import net.adoptium.marketplace.server.frontend.OpenApiDocs
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Tag(name = "Release Info")
@Path("/v1/info/")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
class AvailableReleasesResource
@Inject
constructor(
    private val apiDataStore: APIDataStore
) {
    @GET
    @Path("/available_releases/{vendor}")
    @Operation(summary = "Returns information about available releases", operationId = "getAvailableReleases")
    fun get(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor
    ): ReleaseInfo {
        return runBlocking {
            return@runBlocking apiDataStore.getReleases(vendor).getReleaseInfo()
        }
    }
}
