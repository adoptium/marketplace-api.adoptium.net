package net.adoptium.marketplace.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Asset {

    public static final String SHA_256_SUM_LINK_NAME = "sha256sum_link";
    public static final String SIGNATURE_LINK_NAME = "signature_link";


    public static final String SHA256SUM_NAME = "sha256sum";

    // Name of previous typo to be removed when vendors update their code to fix the typo
    public static final String SHA265SUM_NAME = "sha265sum";

    @Schema(examples = {"OpenJDK8U-jre_x86-32_windows_hotspot_8u212b04.msi"}, required = true)
    private final String name;

    @Schema(examples = {"https://github.com/AdoptOpenJDK/openjdk8-binaries/ga/download/jdk8u212-b04/OpenJDK8U-jre_x86-32_windows_hotspot_8u212b04.msi"}, required = true)
    private final String link;

    @Schema(examples = {"dd28d6d2cde2b931caf94ac2422a2ad082ea62f0beee3bf7057317c53093de93"}, required = true)
    private final String sha256sum;

    @Schema(
            examples = {"dd28d6d2cde2b931caf94ac2422a2ad082ea62f0beee3bf7057317c53093de93"},
            description = "This value is present to avoid an API breaking change from a previous typo for sha256sum in the API. DO NOT USE THIS, it is deprecated and will be removed.",
            deprecated = true)
    private final String sha265sum;

    @Schema(
            examples = {"https://github.com/AdoptOpenJDK/openjdk8-openj9-releases/ga/download/jdk8u162-b12_openj9-0.8.0/OpenJDK8-OPENJ9_x64_Linux_jdk8u162-b12_openj9-0.8.0.tar.gz.sha256.txt"},
            name = SHA_256_SUM_LINK_NAME
    )
    private final String sha256sumLink;

    @Schema(
            examples = {"https://github.com/AdoptOpenJDK/openjdk11-upstream-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jdk_x64_linux_11.0.5_10.tar.gz.sign"},
            name = SIGNATURE_LINK_NAME
    )
    private final String signatureLink;

    @JsonCreator
    public Asset(
            @JsonProperty("name") String name,
            @JsonProperty("link") String link,
            @JsonProperty(SHA256SUM_NAME) String sha256sum,

            // TODO: Remove this when vendors update their code to fix the typo
            @JsonProperty(SHA265SUM_NAME) String sha265sum,

            @JsonProperty(SHA_256_SUM_LINK_NAME) String sha256sumLink,
            @JsonProperty(SIGNATURE_LINK_NAME) String signatureLink) {
        this.name = name;
        this.link = link;

        // TODO: Remove this when vendors update their code to fix the typo
        if (sha256sum != null) {
            this.sha256sum = sha256sum;
            this.sha265sum = sha256sum;
        } else {
            this.sha256sum = sha265sum;
            this.sha265sum = sha265sum;
        }

        this.sha256sumLink = sha256sumLink;
        this.signatureLink = signatureLink;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    @JsonProperty(SHA256SUM_NAME)
    public String getSha256sum() {
        return sha256sum;
    }

    @JsonProperty(SHA265SUM_NAME)
    public String getSha265sum() {
        return sha265sum;
    }

    @JsonProperty(SHA_256_SUM_LINK_NAME)
    public String getSha256sumLink() {
        return sha256sumLink;
    }

    @JsonProperty(SIGNATURE_LINK_NAME)
    public String getSignatureLink() {
        return signatureLink;
    }
}
