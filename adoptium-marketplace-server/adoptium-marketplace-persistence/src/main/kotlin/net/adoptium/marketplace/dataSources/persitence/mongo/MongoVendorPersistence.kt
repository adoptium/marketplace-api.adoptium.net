package net.adoptium.marketplace.dataSources.persitence.mongo

import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.adoptium.marketplace.dataSources.ReleaseInfo
import net.adoptium.marketplace.dataSources.TimeSource
import net.adoptium.marketplace.dataSources.persitence.VendorPersistence
import net.adoptium.marketplace.schema.*
import org.bson.*
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

open class MongoVendorPersistence constructor(
    mongoClient: MongoClient,
    private val vendor: Vendor
) : MongoInterface(), VendorPersistence {

    private val releasesCollection: MongoCollection<Release> =
        createCollection(mongoClient.getDatabase(), vendor.name + "_" + RELEASE_DB)
    private val releaseInfoCollection: MongoCollection<ReleaseInfo> =
        createCollection(mongoClient.getDatabase(), vendor.name + "_" + RELEASE_INFO_DB)
    private val updateTimeCollection: MongoCollection<UpdatedInfo> = createCollection(
        mongoClient.getDatabase(),
        vendor.name + "_" + UPDATE_TIME_DB,
        MongoVendorPersistence::initUptimeDb
    )
    private val updateLogCollection: MongoCollection<ReleaseUpdateInfo> =
        createCollection(mongoClient.getDatabase(), vendor.name + "_" + UPDATE_LOG)

    private val codecs = mongoClient.getCodecs();

    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        const val RELEASE_DB = "release"
        const val RELEASE_INFO_DB = "releaseInfo"
        const val UPDATE_TIME_DB = "updateTime"
        const val UPDATE_LOG = "updateLog"

        fun initUptimeDb(collection: MongoCollection<UpdatedInfo>) {
            runBlocking {
                try {
                    collection.insertOne(UpdatedInfo(Date.from(TimeSource.now().minusMinutes(5).toInstant())))
                } catch (e: Exception) {
                    LOGGER.error("Failed to run init", e)
                }
            }
        }
    }

    override suspend fun writeReleases(releases: ReleaseList): ReleaseUpdateInfo {

        val added = mutableListOf<Release>()
        val updated = mutableListOf<Release>()

        releases
            .releases
            .filter { it.vendor == vendor }
            .forEach { release ->

                val matcher = releaseMatcher(release)

                val docCount = releasesCollection
                    .countDocuments(matcher, CountOptions())

                if (docCount > 1) {
                    LOGGER.warn("MULTIPLE DOCUMENTS MATCH $vendor ${release.releaseName} ${release.releaseLink} ${release.openjdkVersionData}. This might cause issues.")
                }

                val result = upsertValue(matcher, release, releasesCollection)

                if (result.upsertedId != null) {
                    added.add(release)
                } else {
                    if (result.modifiedCount > 0) {
                        updated.add(release)
                    }
                }
            }

        val currentDb = this.getAllReleases()

        val removed = currentDb
            .releases
            .filter { currentRelease ->
                releases.releases.none {
                    it.vendor == currentRelease.vendor &&
                            it.releaseName == currentRelease.releaseName &&
                            it.releaseLink == currentRelease.releaseLink &&
                            it.openjdkVersionData.compareTo(currentRelease.openjdkVersionData) == 0
                }
            }
            .map { toRemove ->
                LOGGER.info("Removing old release ${toRemove.releaseName}")
                val matcher = releaseMatcher(toRemove)
                val deleted = releasesCollection.deleteMany(matcher)
                if (deleted.deletedCount != 1L) {
                    LOGGER.error("Failed to delete release ${toRemove.releaseName}")
                }
                return@map toRemove
            }

        if (added.isNotEmpty() || updated.isNotEmpty() || removed.isNotEmpty()) {
            updateUpdatedTime(Date())
        }

        val result = ReleaseUpdateInfo(ReleaseList(added), ReleaseList(updated), ReleaseList(removed), Date())
        logUpdate(result)
        return result
    }

    private suspend fun <T : Any> upsertValue(
        matcher: Bson,
        value: T?,
        collection: MongoCollection<T>
    ): UpdateResult {
        val doc = BsonDocument()
        doc["\$set"] = BsonDocumentWrapper.asBsonDocument(value, codecs)

        return collection
            .updateOne(
                matcher,
                doc,
                UpdateOptions().upsert(true)
            )
    }

    private suspend fun logUpdate(result: ReleaseUpdateInfo) {
        updateLogCollection.insertOne(result)
        updateTimeCollection.deleteMany(
            Document(
                "timestamp",
                BsonDocument(
                    "\$lt",
                    BsonDateTime(result.timestamp.toInstant().minus(Duration.ofDays(30)).toEpochMilli())
                )
            )
        )
    }

    override suspend fun setReleaseInfo(releaseInfo: ReleaseInfo) {
        releaseInfoCollection.deleteMany(releaseVersionDbEntryMatcher())


        upsertValue(
            releaseVersionDbEntryMatcher(),
            releaseInfo,
            releaseInfoCollection
        )
    }

    private suspend fun updateUpdatedTime(dateTime: Date) {
        upsertValue(
            Document(),
            UpdatedInfo(dateTime),
            updateTimeCollection
        )
        updateTimeCollection.deleteMany(
            Document(
                "time",
                BsonDocument("\$lt", BsonDateTime(dateTime.toInstant().toEpochMilli()))
            )
        )
    }

    override suspend fun getUpdatedInfoIfUpdatedSince(since: Date): UpdatedInfo? {
        val result = updateTimeCollection.find(
            Document(
                "time",
                BsonDocument("\$gt", BsonDateTime(since.toInstant().toEpochMilli()))
            )
        )

        return result.firstOrNull()
    }

    override suspend fun getReleaseVendorStatus(): List<ReleaseUpdateInfo> {
        return updateLogCollection.find().toList()
    }

    override suspend fun getReleaseInfo(): ReleaseInfo? {
        return releaseInfoCollection.find(releaseVersionDbEntryMatcher()).first()
    }

    override suspend fun getAllReleases(): ReleaseList {
        return ReleaseList(releasesCollection.find().toList())
    }

    private fun releaseVersionDbEntryMatcher() = Document("tip_version", BsonDocument("\$exists", BsonBoolean(true)))

    private fun releaseMatcher(release: Release): BsonDocument {

        var matcher = listOf(
            BsonElement("release_name", BsonString(release.releaseName)),
            BsonElement("vendor", BsonString(release.vendor.name))
        )

        if (release.releaseLink != null) {
            matcher = matcher.plus(BsonElement("release_link", BsonString(release.releaseLink)));
        }

        return BsonDocument(
            matcher
                .plus(versionMatcher(release.openjdkVersionData))
        )
    }

    private fun versionMatcher(openjdkVersionData: OpenjdkVersionData): List<BsonElement> {
        var matcher = listOf(
            BsonElement("openjdk_version_data.openjdk_version", BsonString(openjdkVersionData.openjdk_version)),
            BsonElement("openjdk_version_data.major", BsonInt32(openjdkVersionData.major))
        )

        if (openjdkVersionData.build.isPresent) {
            matcher = matcher.plus(BsonElement("openjdk_version_data.build", BsonInt32(openjdkVersionData.build.get())))
        }
        if (openjdkVersionData.minor.isPresent) {
            matcher = matcher.plus(BsonElement("openjdk_version_data.minor", BsonInt32(openjdkVersionData.minor.get())))
        }
        if (openjdkVersionData.pre.isPresent) {
            matcher = matcher.plus(BsonElement("openjdk_version_data.pre", BsonString(openjdkVersionData.pre.get())))
        }
        if (openjdkVersionData.optional.isPresent) {
            matcher = matcher.plus(
                BsonElement(
                    "openjdk_version_data.optional",
                    BsonString(openjdkVersionData.optional.get())
                )
            )
        }
        if (openjdkVersionData.patch.isPresent) {
            matcher = matcher.plus(BsonElement("openjdk_version_data.patch", BsonInt32(openjdkVersionData.patch.get())))
        }
        if (openjdkVersionData.security.isPresent) {
            matcher =
                matcher.plus(BsonElement("openjdk_version_data.security", BsonInt32(openjdkVersionData.security.get())))
        }

        return matcher
    }
}
