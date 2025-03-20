package net.adoptium.api

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.mockk.MockKAnnotations
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Alternative
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.runBlocking
import net.adoptium.marketplace.dataSources.persitence.mongo.codecs.JacksonCodecProvider
import net.adoptium.marketplace.dataSources.persitence.mongo.codecs.ZonedDateTimeCodecProvider
import net.adoptium.marketplace.client.TestServer
import net.adoptium.marketplace.dataSources.APIDataStore
import net.adoptium.marketplace.dataSources.APIDataStoreImpl
import net.adoptium.marketplace.dataSources.VendorReleases
import net.adoptium.marketplace.dataSources.VendorReleasesFactoryImpl
import net.adoptium.marketplace.dataSources.persitence.DefaultVendorPersistenceFactory
import net.adoptium.marketplace.dataSources.persitence.mongo.MongoClient
import net.adoptium.marketplace.dataSources.persitence.mongo.MongoTest
import net.adoptium.marketplace.schema.Vendor
import net.adoptium.marketplace.server.updater.VendorInfo
import net.adoptium.marketplace.server.updater.VendorList
import org.bson.codecs.configuration.CodecRegistries
import org.jboss.weld.junit5.auto.AddPackages
import org.jboss.weld.junit5.auto.EnableAlternatives
import org.jboss.weld.junit5.auto.EnableAutoWeld
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Priority(1)
@Alternative
@ApplicationScoped
class MockVendorList : VendorList {
    override fun getVendorInfo(): Map<Vendor, VendorInfo> {
        return mapOf(
            Vendor.adoptium to VendorInfo(
                Vendor.adoptium,
                "http://localhost:" + TestServer.PORT + "/workingRepository",
                "../../../exampleRepositories/keys/public.pem"
            )
        )
    }
}

@Priority(1)
@Alternative
@ApplicationScoped
class FongoClient : MongoClient() {

    private val settingsBuilder: MongoClientSettings.Builder = MongoClientSettings.builder()
        .codecRegistry(
            CodecRegistries.fromProviders(
                MongoClientSettings.getDefaultCodecRegistry(),
                ZonedDateTimeCodecProvider(),
                JacksonCodecProvider()
            )
        )
        .applyConnectionString(ConnectionString(System.getProperty("MONGODB_TEST_CONNECTION_STRING")))

    private val db: MongoDatabase

    init {
        val client = com.mongodb.kotlin.client.coroutine.MongoClient.create(settingsBuilder.build())
        db = client.getDatabase("test-api")
    }

    override fun getDatabase(): MongoDatabase {
        return db
    }
}

@QuarkusTest
@EnableAlternatives
@EnableAutoWeld
@ExtendWith(
    value = [
        TestServer::class,
        MongoTest::class
    ]
)
@AddPackages(
    value = [
        APIDataStore::class,
        VendorList::class
    ]
)
class UpdateTriggerTest {

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Disabled("Unreliable test")
    @Test
    fun `db is updated`() {
        runBlocking {
            VendorReleases.UPDATE_COOLOFF_IN_SECONDS = 0

            val apiDataStore =
                APIDataStoreImpl(VendorReleasesFactoryImpl(DefaultVendorPersistenceFactory(FongoClient())))

            var releases = apiDataStore.getReleases(Vendor.adoptium).getAllReleases()
            Assertions.assertTrue(releases.releases.size == 0)

            getRequest()
                .`when`()
                .get("/updateForVendor/adoptium")
                .then()
                .statusCode(Response.Status.OK.statusCode)

            releases = apiDataStore.getReleases(Vendor.adoptium).getAllReleases()
            Assertions.assertTrue(releases.releases.size > 0)
        }
    }

    @Test
    fun `user is restricted to their own vendor`() {
        RestAssured.given()
            .auth()
            .preemptive()
            .basic("otherTestUpdaterUser", "a-test-token")
            .`when`()
            .get("/updateForVendor/adoptium")
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    fun `bad password is rejected`() {
        RestAssured.given()
            .auth()
            .preemptive()
            .basic("testUpdaterUser", "badPasswd")
            .`when`()
            .get("/updateForVendor/adoptium")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    private fun getRequest() = RestAssured.given()
        .auth()
        .preemptive()
        .basic("testUpdaterUser", "a-test-token")
        .`when`()
}
