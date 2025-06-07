package net.adoptium.marketplace.server.frontend.routes

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.runBlocking
import net.adoptium.marketplace.dataSources.APIDataStore
import net.adoptium.marketplace.schema.*
import net.adoptium.marketplace.server.frontend.OpenApiDocs
import net.adoptium.marketplace.server.frontend.Pagination.defaultPageSize
import net.adoptium.marketplace.server.frontend.Pagination.getPage
import net.adoptium.marketplace.server.frontend.Pagination.maxPageSize
import net.adoptium.marketplace.server.frontend.filters.BinaryFilter
import net.adoptium.marketplace.server.frontend.filters.BinaryFilterMultiple
import net.adoptium.marketplace.server.frontend.filters.ReleaseFilter
import net.adoptium.marketplace.server.frontend.filters.ReleaseFilterMultiple
import net.adoptium.marketplace.server.frontend.models.APIDateTime
import net.adoptium.marketplace.server.frontend.models.BinaryAssetView
import net.adoptium.marketplace.server.frontend.models.SortMethod
import net.adoptium.marketplace.server.frontend.models.SortOrder
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Tag(name = "Assets")
@Path("/v1/assets/")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
class AssetsResource
@Inject
constructor(
    private val releaseEndpoint: ReleaseEndpoint,
    private val apiDataStore: APIDataStore
) {
    @GET
    @Path("/feature_releases/{vendor}/{feature_version}")
    @Operation(
        operationId = "searchReleases",
        summary = "Returns release information",
        description = "List of information about builds that match the current query"
    )
    @APIResponses(
        value = [
            APIResponse(
                responseCode = "200", description = "search results matching criteria",
                content = [Content(schema = Schema(type = SchemaType.ARRAY, implementation = Release::class))]
            ),
            APIResponse(responseCode = "400", description = "bad input parameter")
        ]
    )
    fun get(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor,

        @Parameter(
            name = "feature_version", description = OpenApiDocs.FEATURE_RELEASE, required = true,
            schema = Schema(examples = ["8"], type = SchemaType.INTEGER)
        )
        @PathParam("feature_version")
        version: Int,

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

        @Parameter(
            name = "before",
            description = "<p>Return binaries whose updated_at is before the given date/time. When a date is given the match is inclusive of the given day. <ul> <li>2020-01-21</li> <li>2020-01-21T10:15:30</li> <li>20200121</li> <li>2020-12-21T10:15:30Z</li> <li>2020-12-21+01:00</li> </ul></p> ",
            required = false
        )
        @QueryParam("before")
        before: APIDateTime?,

        @Parameter(
            name = "page_size",
            description = "Pagination page size",
            schema = Schema(defaultValue = defaultPageSize, maximum = maxPageSize, type = SchemaType.INTEGER),
            required = false
        )
        @DefaultValue(defaultPageSize)
        @QueryParam("page_size")
        pageSize: Int,

        @Parameter(
            name = "page", description = "Pagination page number",
            schema = Schema(defaultValue = "0", type = SchemaType.INTEGER), required = false
        )
        @DefaultValue("0")
        @QueryParam("page")
        page: Int,

        @Parameter(name = "sort_order", description = "Result sort order", required = false)
        @QueryParam("sort_order")
        @DefaultValue(SortOrder.DEFAULT_SORT_ORDER)
        sortOrder: SortOrder,

        @Parameter(name = "sort_method", description = "Result sort method", required = false)
        @QueryParam("sort_method")
        @DefaultValue(SortMethod.DEFAULT_SORT_METHOD)
        sortMethod: SortMethod

    ): List<Release> {

        return runBlocking {
            val order = sortOrder
            val releaseSortMethod = sortMethod

            val releaseFilter = ReleaseFilter(featureVersion = version)
            val binaryFilter = BinaryFilter(os, arch, image_type, jvm_impl, before, cLib)

            val releases = releaseEndpoint.getReleases(vendor, releaseFilter, binaryFilter, order, releaseSortMethod)

            return@runBlocking getPage(pageSize, page, releases)
        }
    }

    @GET
    @Path("/release_name/{vendor}/{release_name}")
    @Operation(
        operationId = "getReleaseInfo",
        summary = "Returns release information",
        description = "List of releases with the given release name"
    )
    @APIResponses(
        value = [
            APIResponse(
                responseCode = "200", description = "Release with the given vendor and name"
            ),
            APIResponse(responseCode = "400", description = "bad input parameter"),
            APIResponse(responseCode = "404", description = "no releases match the request"),
            APIResponse(responseCode = "500", description = "multiple releases match the request")
        ]
    )
    fun get(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor,

        @Parameter(name = "release_name", description = "Name of the release i.e ", required = true)
        @PathParam("release_name")
        releaseName: String,

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
        jvm_impl: JvmImpl?
    ): Release {
        return runBlocking {
            if (releaseName.trim().isEmpty()) {
                throw BadRequestException("Must provide a releaseName")
            }

            val releaseFilter = ReleaseFilter(vendor = vendor, releaseName = releaseName.trim())
            val binaryFilter = BinaryFilter(os, arch, image_type, jvm_impl, null, cLib)

            val releases =
                releaseEndpoint.getReleases(vendor, releaseFilter, binaryFilter, SortOrder.DESC, SortMethod.DEFAULT)
                    .toList()

            return@runBlocking when {
                releases.isEmpty() -> {
                    throw NotFoundException("No releases found")
                }

                releases.size > 1 -> {
                    throw ServerErrorException("Multiple releases match request", Response.Status.INTERNAL_SERVER_ERROR)
                }

                else -> {
                    releases[0]
                }
            }
        }
    }

    @GET
    @Path("/version/{vendor}/{version}")
    @Operation(
        operationId = "searchReleasesByVersion",
        summary = "Returns release information about the specified version.",
        description = "List of information about builds that match the current query "
    )
    @APIResponses(
        value = [
            APIResponse(
                responseCode = "200", description = "search results matching criteria",
                content = [Content(schema = Schema(type = SchemaType.ARRAY, implementation = Release::class))]
            ),
            APIResponse(responseCode = "400", description = "bad input parameter")
        ]
    )
    fun getReleaseVersion(
        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor,

        @Parameter(name = "version", description = OpenApiDocs.VERSION_RANGE, required = true)
        @PathParam("version")
        version: String,

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
        lts: Optional<Boolean>,

        @Parameter(
            name = "page_size",
            description = "Pagination page size",
            schema = Schema(defaultValue = defaultPageSize, maximum = maxPageSize, type = SchemaType.INTEGER),
            required = false
        )
        @DefaultValue(defaultPageSize)
        @QueryParam("page_size")
        pageSize: Int,

        @Parameter(
            name = "page", description = "Pagination page number",
            schema = Schema(defaultValue = "0", type = SchemaType.INTEGER), required = false
        )
        @DefaultValue("0")
        @QueryParam("page")
        page: Int,

        @Parameter(name = "sort_order", description = "Result sort order", required = false)
        @QueryParam("sort_order")
        @DefaultValue(SortOrder.DEFAULT_SORT_ORDER)
        sortOrder: SortOrder,

        @Parameter(name = "sort_method", description = "Result sort method", required = false)
        @QueryParam("sort_method")
        @DefaultValue(SortMethod.DEFAULT_SORT_METHOD)
        sortMethod: SortMethod
    ): List<Release> {
        return runBlocking {
            val releases = releaseEndpoint.getReleases(
                vendor,
                sortOrder,
                sortMethod,
                version,
                lts.getOrNull(),
                os,
                arch,
                image_type,
                jvm_impl,
                cLib
            )
            return@runBlocking getPage(pageSize, page, releases)
        }
    }

    data class binaryPermutation(
        val vendor: Vendor,
        val arch: Architecture,
        val imageType: ImageType,
        val os: OperatingSystem
    )

    @GET
    @Path("/latest/{vendor}/{feature_version}/{jvm_impl}")
    @Operation(
        summary = "Returns list of latest assets for the given feature version and jvm impl",
        operationId = "getLatestAssets"
    )
    fun getLatestAssets(

        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @PathParam("vendor")
        vendor: Vendor,

        @Parameter(
            name = "feature_version", description = OpenApiDocs.FEATURE_RELEASE, required = true,
            schema = Schema(examples = ["8"], type = SchemaType.INTEGER)
        )
        @PathParam("feature_version")
        version: Int,

        @Parameter(name = "jvm_impl", description = "JVM Implementation", required = true)
        @PathParam("jvm_impl")
        jvm_impl: JvmImpl,


        ): List<BinaryAssetView> {
        return runBlocking {
            val releaseFilter = ReleaseFilter(featureVersion = version)
            val binaryFilter = BinaryFilter(null, null, null, jvm_impl, null, null)

            val releases =
                releaseEndpoint.getReleases(vendor, releaseFilter, binaryFilter, SortOrder.ASC, SortMethod.DEFAULT)

            return@runBlocking releases
                .flatMap { release ->
                    release.binaries
                        .asSequence()
                        .map { Pair(release, it) }
                }
                .associateBy {
                    binaryPermutation(it.first.vendor, it.second.architecture, it.second.imageType, it.second.os)
                }
                .values
                .map { BinaryAssetView(it.first.releaseName, it.first.vendor, it.second, it.first.openjdkVersionData) }
                .toList()
        }
    }


    @GET
    @Path("/latestForVendors")
    @Operation(
        summary = "Returns list of latest assets for the given feature version and jvm impl",
        operationId = "latestForVendors"
    )
    fun latestForVendors(

        @Parameter(name = "vendor", description = OpenApiDocs.VENDOR, required = true)
        @QueryParam("vendor")
        vendors: List<Vendor>,

        @Parameter(name = "os", description = "Operating System", required = false)
        @QueryParam("os")
        os: Optional<List<OperatingSystem>>,

        @Parameter(name = "architecture", description = "Architecture", required = false)
        @QueryParam("architecture")
        arch: Optional<List<Architecture>>,

        @Parameter(name = "image_type", description = "Image Type", required = false)
        @QueryParam("image_type")
        image_type: Optional<List<ImageType>>,

        @Parameter(name = "feature_version", description = OpenApiDocs.FEATURE_RELEASE, required = false)
        @QueryParam("feature_version")
        version: Optional<List<Int>>

    ): List<BinaryAssetView> {

        return runBlocking {
            val versions = if (version.isEmpty || version.get().isEmpty()) {
                apiDataStore.getReleases(Vendor.adoptium).getReleaseInfo().available_releases.toList()
            } else {
                version.getOrNull()
            }

            return@runBlocking vendors
                .flatMap { vendor ->
                    val releaseFilter = ReleaseFilterMultiple(versions, null, listOf(vendor), null)
                    val binaryFilter =
                        BinaryFilterMultiple(os.getOrNull(), arch.getOrNull(), image_type.getOrNull(), null, null, null)

                    releaseEndpoint.getReleases(vendor, releaseFilter, binaryFilter, SortOrder.ASC, SortMethod.DEFAULT)
                }
                .flatMap { release ->
                    release.binaries
                        .asSequence()
                        .map { Pair(release, it) }
                }
                .associateBy {
                    binaryPermutation(it.first.vendor, it.second.architecture, it.second.imageType, it.second.os)
                }
                .values
                .map { BinaryAssetView(it.first.releaseName, it.first.vendor, it.second, it.first.openjdkVersionData) }
                .toList()
        }
    }
}
