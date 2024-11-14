package net.adoptium.marketplace.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Package extends Asset {

    public Package(
            String name,
            String link,
            String sha256sum,
            String sha256sumLink,
            String signatureLink
    ) {
        super(name, link, sha256sum, sha256sum, sha256sumLink, signatureLink);
    }

    @JsonCreator
    public Package(
            @JsonProperty("name") String name,
            @JsonProperty("link") String link,
            @JsonProperty(Asset.SHA256SUM_NAME) String sha256sum,
            @JsonProperty(Asset.SHA265SUM_NAME) String sha265sum,
            @JsonProperty(Asset.SHA_256_SUM_LINK_NAME) String sha256sumLink,
            @JsonProperty(Asset.SIGNATURE_LINK_NAME) String signatureLink
    ) {
        super(name, link, sha256sum, sha265sum, sha256sumLink, signatureLink);
    }
}
