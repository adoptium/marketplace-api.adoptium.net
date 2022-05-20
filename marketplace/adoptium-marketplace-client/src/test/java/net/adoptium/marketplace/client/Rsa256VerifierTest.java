package net.adoptium.marketplace.client;

import net.adoptium.marketplace.client.signature.Rsa256SignatureVerify;
import net.adoptium.marketplace.client.signature.SignatureVerifier;
import net.adoptium.marketplace.schema.ReleaseList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;


@ExtendWith({TestServer.class})
public class Rsa256VerifierTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Rsa256VerifierTest.class.getName());

    private static final String KEY;

    static {
        String key;

        try {
            key = new String(new FileInputStream("../exampleRepositories/keys/public.pem").readAllBytes());
        } catch (IOException e) {
            key = null;
            LOGGER.error("Failed to read key", e);
        }
        KEY = key;
    }

    @Test
    public void pullFullRepository() throws Exception, FailedToPullDataException {
        MarketplaceClient client = getMarketplaceClient("http://localhost:8090/workingRepository");

        ReleaseList releases = client.readRepositoryData();
        Assertions.assertFalse(releases.getReleases().isEmpty());
    }

    @Test
    public void pullFullRepositoryWithBadSignatures() throws Exception, FailedToPullDataException {
        MarketplaceClient client = getMarketplaceClient("http://localhost:8090/repositoryWithBadSignatures");

        ReleaseList releases = client.readRepositoryData();

        // jdk8u302-b08 is the only release with a valid signature
        Assertions.assertEquals(1, releases.getReleases().size());
        Assertions.assertEquals("jdk8u302-b08", releases.getReleases().get(0).getReleaseName());
    }

    public MarketplaceClient getMarketplaceClient(String url) throws Exception {
        SignatureVerifier sv = Rsa256SignatureVerify.build(KEY);
        return new MarketplaceClient(url, MarketplaceHttpClient.build(sv));
    }
}