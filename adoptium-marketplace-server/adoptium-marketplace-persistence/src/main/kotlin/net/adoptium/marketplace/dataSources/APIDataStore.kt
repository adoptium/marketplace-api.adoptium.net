package net.adoptium.marketplace.dataSources

import net.adoptium.marketplace.schema.Vendor
import jakarta.inject.Inject
import jakarta.enterprise.context.ApplicationScoped

interface APIDataStore {
    fun getReleases(vendor: Vendor): VendorReleases
}

@ApplicationScoped
class APIDataStoreImpl @Inject constructor(private val vendorReleasesFactory: VendorReleasesFactory) : APIDataStore {

    private val vendorReleases: MutableMap<Vendor, VendorReleases> = mutableMapOf()

    private fun getVendorReleases(vendor: Vendor): VendorReleases {
        if (!vendorReleases.containsKey(vendor)) {
            vendorReleases[vendor] = vendorReleasesFactory.get(vendor)
        }

        return vendorReleases[vendor]!!
    }

    override fun getReleases(vendor: Vendor): VendorReleases {
        return getVendorReleases(vendor)
    }
}
