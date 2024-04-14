package net.adoptium.marketplace.server.frontend.routes.info

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.runBlocking
import net.adoptium.marketplace.schema.*
import net.adoptium.marketplace.server.frontend.OpenApiDocs
import net.adoptium.marketplace.server.frontend.Pagination
import net.adoptium.marketplace.server.frontend.Pagination.getPage
import net.adoptium.marketplace.server.frontend.models.ReleaseNameList
import net.adoptium.marketplace.server.frontend.models.ReleaseVersionList
import net.adoptium.marketplace.server.frontend.models.SortMethod
import net.adoptium.marketplace.server.frontend.models.SortOrder
import net.adoptium.marketplace.server.frontend.routes.ReleaseEndpoint
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Tag(name = "Release Info")
@Path("/v1/info")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
class ReleaseListResource
@Inject
constructor(
    private val releaseEndpoint: ReleaseEndpoint
) {

    @GET
    @Path("/release_names/{vendor}")
    @Operation(summary = "Returns a list of all release names", operationId = "getReleaseNames")
    fun get(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor,

        @Parameter(name = "version", description = OpenApiDocs.VERSION_RANGE, required = false)
        @QueryParam("version")
        version: String?,

        @Parameter(name = "os", description = "Operating System", required = false)
        @QueryParam("os")
        os: OperatingSystem?,

        @Parameter(name = "architecture", description = "Architecture", required = false)
        @QueryParam("architecture")
        arch: Architecture?,

        @Parameter(name = "image_type", description = "Image Type", required = false)
        @QueryParam("image_type")
        image_type: ImageType?,

        @Parameter(name = "c_lib", description = OpenApiDocs.CLIB_TYPE, required = false)
        @QueryParam("c_lib")
        cLib: CLib?,

        @Parameter(name = "jvm_impl", description = "JVM Implementation", required = false)
        @QueryParam("jvm_impl")
        jvm_impl: JvmImpl?,

        @Parameter(name = "lts", description = "Include only LTS releases", required = false)
        @QueryParam("lts")
        lts: Boolean?,

        @Parameter(
            name = "page_size",
            description = "Pagination page size",
            schema = Schema(
                defaultValue = Pagination.defaultPageSize,
                maximum = Pagination.maxPageSize,
                type = SchemaType.INTEGER
            ),
            required = false
        )
        @QueryParam("page_size")
        pageSize: Int?,

        @Parameter(
            name = "page",
            description = "Pagination page number",
            schema = Schema(defaultValue = "0", type = SchemaType.INTEGER),
            required = false
        )
        @QueryParam("page")
        page: Int?,

        @Parameter(name = "sort_order", description = "Result sort order", required = false)
        @QueryParam("sort_order")
        sortOrder: SortOrder?,

        @Parameter(name = "sort_method", description = "Result sort method", required = false)
        @QueryParam("sort_method")
        sortMethod: SortMethod?
    ): ReleaseNameList {
        return runBlocking {
            val releases = releaseEndpoint.getReleases(
                vendor,
                sortOrder,
                sortMethod,
                version,
                lts,
                os,
                arch,
                image_type,
                jvm_impl,
                cLib
            )
                .map { it.releaseName }

            val pagedReleases = getPage(pageSize, page, releases)

            return@runBlocking ReleaseNameList(pagedReleases)
        }
    }

    @Path("/release_versions/{vendor}")
    @GET
    @Operation(summary = "Returns a list of all release versions", operationId = "getReleaseVersions")
    fun getVersions(

        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor,

        @Parameter(name = "version", description = OpenApiDocs.VERSION_RANGE, required = false)
        @QueryParam("version")
        version: String?,

        @Parameter(name = "os", description = "Operating System", required = false)
        @QueryParam("os")
        os: OperatingSystem?,

        @Parameter(name = "architecture", description = "Architecture", required = false)
        @QueryParam("architecture")
        arch: Architecture?,

        @Parameter(name = "image_type", description = "Image Type", required = false)
        @QueryParam("image_type")
        image_type: ImageType?,

        @Parameter(name = "c_lib", description = OpenApiDocs.CLIB_TYPE, required = false)
        @QueryParam("c_lib")
        cLib: CLib?,

        @Parameter(name = "jvm_impl", description = "JVM Implementation", required = false)
        @QueryParam("jvm_impl")
        jvm_impl: JvmImpl?,

        @Parameter(name = "lts", description = "Include only LTS releases", required = false)
        @QueryParam("lts")
        lts: Boolean?,

        @Parameter(
            name = "page_size",
            description = "Pagination page size",
            schema = Schema(
                defaultValue = Pagination.defaultPageSize,
                maximum = Pagination.largerPageSize,
                type = SchemaType.INTEGER
            ),
            required = false
        )
        @QueryParam("page_size")
        pageSize: Int?,

        @Parameter(
            name = "page",
            description = "Pagination page number",
            schema = Schema(defaultValue = "0", type = SchemaType.INTEGER),
            required = false
        )
        @QueryParam("page")
        page: Int?,

        @Parameter(name = "sort_order", description = "Result sort order", required = false)
        @QueryParam("sort_order")
        sortOrder: SortOrder?,

        @Parameter(name = "sort_method", description = "Result sort method", required = false)
        @QueryParam("sort_method")
        sortMethod: SortMethod?

    ): ReleaseVersionList {
        return runBlocking {
            val releases = releaseEndpoint.getReleases(
                vendor,
                sortOrder,
                sortMethod,
                version,
                lts,
                os,
                arch,
                image_type,
                jvm_impl,
                cLib
            )
                .map { it.openjdkVersionData }
                .distinct()

            val pagedReleases = getPage(pageSize, page, releases, maxPageSizeNum = Pagination.largerPageSizeNum)

            return@runBlocking ReleaseVersionList(pagedReleases.toTypedArray())
        }
    }
}
