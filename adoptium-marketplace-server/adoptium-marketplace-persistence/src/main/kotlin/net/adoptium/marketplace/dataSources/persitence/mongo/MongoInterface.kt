package net.adoptium.marketplace.dataSources.persitence.mongo

import com.mongodb.MongoCommandException
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

abstract class MongoInterface {
    companion object {
        @JvmStatic
        val LOGGER = LoggerFactory.getLogger(this::class.java)
    }

    inline fun <reified T : Any> createCollection(
        database: MongoDatabase,
        collectionName: String,
        crossinline onCollectionCreated: ((MongoCollection<T>) -> Unit) = {}
    ): MongoCollection<T> {
        runBlocking {
            try {
                database.createCollection(collectionName)
                val collection = database.getCollection<T>(collectionName)
                onCollectionCreated(collection)
            } catch (e: MongoCommandException) {
                if (e.errorCode == 48) {
                    // collection already exists ... ignore
                } else {
                    LOGGER.warn(
                        "User does not have permission to create collection $collectionName",
                        e
                    )
                }
            }
        }
        return database.getCollection(collectionName, T::class.java)

    }
}
