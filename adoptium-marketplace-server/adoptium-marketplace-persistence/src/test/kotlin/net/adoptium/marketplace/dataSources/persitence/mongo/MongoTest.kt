package net.adoptium.marketplace.dataSources.persitence.mongo

import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.reverse.transitions.Start
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory

class MongoTest : BeforeAllCallback, AfterAllCallback {

    companion object {
        private var mongodExecutable: RunningMongodProcess? = null

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        fun startFongo() {
            val bindIp = "localhost"
            val net = Net.of("localhost",
                Network.freeServerPort(Network.getLocalHost()),
                Network.localhostIsIPv6()
            )

            val mongodbTestConnectionString = "mongodb://$bindIp:${net.port}"
            LOGGER.info("Mongo test connection string - $mongodbTestConnectionString")
            System.setProperty("MONGODB_TEST_CONNECTION_STRING", mongodbTestConnectionString)

            mongodExecutable = Mongod.instance().withNet(Start.to(Net::class.java).initializedWith(net)).start(Version.V4_4_18).current()

            LOGGER.info("FMongo started")
        }
    }

    override fun beforeAll(p0: ExtensionContext?) {
        System.setProperty("GITHUB_TOKEN", "stub-token")
        startFongo()
    }

    override fun afterAll(p0: ExtensionContext?) {
        mongodExecutable!!.stop()
    }
}
