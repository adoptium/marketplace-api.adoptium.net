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

        val vendorAnnotation = method.parameters[0].getAnnotation(Parameter::class.java)
        val featureVersionAnnotation = method.parameters[1].getAnnotation(Parameter::class.java)

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
