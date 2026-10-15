package net.adoptium.marketplace.schema;

import net.adoptium.marketplace.client.MarketplaceMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

public class DeserializationTest {

    @Test
    public void canDeserializeDocsWithTypo() throws IOException {
        ReleaseList deserialized = MarketplaceMapper.repositoryObjectMapper.readValue(DeserializationTest.class.getResourceAsStream("example.json"), ReleaseList.class);

        ReleaseList deserializedWithTypo = MarketplaceMapper.repositoryObjectMapper.readValue(DeserializationTest.class.getResourceAsStream("example_with_typo.json"), ReleaseList.class);

        Assertions.assertEquals(
                deserialized.getReleases().get(0).getBinaries().get(0).getPackage().getSha256sum(),
                deserializedWithTypo.getReleases().get(0).getBinaries().get(0).getPackage().getSha256sum());

        Assertions.assertEquals(
                deserialized.getReleases().get(0).getBinaries().get(0).getPackage().getSha265sum(),
                deserializedWithTypo.getReleases().get(0).getBinaries().get(0).getPackage().getSha265sum());

        Assertions.assertNotNull(deserialized.getReleases().get(0).getBinaries().get(0).getPackage().getSha265sum());
        Assertions.assertNotNull(deserialized.getReleases().get(0).getBinaries().get(0).getPackage().getSha256sum());
        Assertions.assertNotNull(deserializedWithTypo.getReleases().get(0).getBinaries().get(0).getPackage().getSha265sum());
        Assertions.assertNotNull(deserializedWithTypo.getReleases().get(0).getBinaries().get(0).getPackage().getSha256sum());
    }

    @Test
    public void canDeserializeExampleDoc() throws IOException {
        ReleaseList deserialized = MarketplaceMapper.repositoryObjectMapper.readValue(DeserializationTest.class.getResourceAsStream("example.json"), ReleaseList.class);

        Assertions.assertNotNull(deserialized);
    }

    @Test
    public void canSerializeThenDeserialize() throws IOException {

        ObjectMapper mapper = MarketplaceMapper.repositoryObjectMapper;

        String serialized = mapper.writeValueAsString(RepoGenerator.generate(""));
        JsonNode release = mapper.readTree(serialized).get("releases").get(0);
        JsonNode versionData = release.get("openjdk_version_data");

        ReleaseList deserialized = mapper.readValue(serialized, ReleaseList.class);

        Assertions.assertDoesNotThrow(() -> Instant.parse(release.get("last_updated_timestamp").asText()));
        Assertions.assertEquals(1, versionData.get("minor").asInt());
        Assertions.assertEquals("foo", versionData.get("optional").asText());
        Assertions.assertNotNull(deserialized);
    }
}
