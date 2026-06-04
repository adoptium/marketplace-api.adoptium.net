package net.adoptium.marketplace.server.frontend.routes

import net.adoptium.marketplace.server.frontend.OpenApiDocs
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AssetsResourceOpenApiTest {

    @Test
    fun `feature release endpoint marks path params as path params`() {
        val method = AssetsResource::class.java.declaredMethods.single { it.name == "getFeatureReleases" }

        val vendorParam = method.parameters.single { it.getAnnotation(jakarta.ws.rs.PathParam::class.java)?.value == "vendor" }
        val featureVersionParam =
            method.parameters.single { it.getAnnotation(jakarta.ws.rs.PathParam::class.java)?.value == "feature_version" }

        val vendorAnnotation = checkNotNull(vendorParam.getAnnotation(Parameter::class.java)) { "@Parameter missing on vendor param" }
        val featureVersionAnnotation = checkNotNull(featureVersionParam.getAnnotation(Parameter::class.java)) { "@Parameter missing on feature_version param" }
        assertEquals("vendor", vendorAnnotation.name)
        assertEquals(ParameterIn.PATH, vendorAnnotation.`in`)
        assertEquals("feature_version", featureVersionAnnotation.name)
        assertEquals(ParameterIn.PATH, featureVersionAnnotation.`in`)
    }

    @Test
    fun `feature release docs link to a valid available releases route`() {
        assertTrue(OpenApiDocs.FEATURE_RELEASE.contains("/v1/info/available_releases/adoptium"))
    }
}
