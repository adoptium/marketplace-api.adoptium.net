package net.adoptium.marketplace.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.adoptium.marketplace.client.MarketplaceMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
    public void canSerializeThenDeserialize() throws JsonProcessingException {

        ObjectMapper mapper = MarketplaceMapper.repositoryObjectMapper;

        String serialized = mapper.writeValueAsString(RepoGenerator.generate(""));

        ReleaseList deserialized = mapper.readValue(serialized, ReleaseList.class);

        Assertions.assertNotNull(deserialized);
    }
}
