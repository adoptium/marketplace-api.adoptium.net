package net.adoptium.marketplace.schema;

import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class SchemaTest {
    @Test
    public void canParseCurrentJsonSchema() throws IOException {
        try (FileInputStream fis = new FileInputStream("../adoptium-marketplace-schema/target/generated/openapi.json")) {
            OpenAPI schema = OpenApiParser.parse(fis, Format.JSON, null);
            Schema version = schema.getComponents().getSchemas().get("OpenjdkVersionData");
            Schema additionalNumbers = version.getProperties().get("additional_version_numbers");
            Assertions.assertEquals(List.of(Schema.SchemaType.ARRAY), additionalNumbers.getType());
            Assertions.assertEquals(List.of(Schema.SchemaType.INTEGER), additionalNumbers.getItems().getType());
            Assertions.assertTrue(version.getRequired() == null || !version.getRequired().contains("additional_version_numbers"));
        }
    }

    @Test
    public void canParseCurrentYamlSchema() throws IOException {
        try (FileInputStream fis = new FileInputStream("../adoptium-marketplace-schema/target/generated/openapi.yaml")) {
            OpenAPI schema = OpenApiParser.parse(fis, Format.YAML, null);
        }
    }
}
