package net.adoptium.marketplace.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Installer extends Asset {

    public static final String INSTALLER_TYPE_NAME = "installer_type";
    @Schema(examples = "msi", description = "Type of the installer, i.e exe, msi, deb, dmg")
    private final String installerType;

    public Installer(
            String name,
            String link,
            String sha256sum,
            String sha256sumLink,
            String signatureLink,
            String installerType
    ) {
        super(name, link, sha256sum, sha256sum, sha256sumLink, signatureLink);
        this.installerType = installerType;
    }

    @JsonCreator
    public Installer(
            @JsonProperty("name") String name,
            @JsonProperty("link") String link,
            @JsonProperty(Asset.SHA256SUM_NAME) String sha256sum,
            @JsonProperty(Asset.SHA265SUM_NAME) String sha265sum,
            @JsonProperty(Asset.SHA_256_SUM_LINK_NAME) String sha256sumLink,
            @JsonProperty(Asset.SIGNATURE_LINK_NAME) String signatureLink,
            @JsonProperty(INSTALLER_TYPE_NAME) String installerType
    ) {
        super(name, link, sha256sum, sha265sum, sha256sumLink, signatureLink);
        this.installerType = installerType;
    }

    @JsonProperty(INSTALLER_TYPE_NAME)
    public String getInstallerType() {
        return installerType;
    }

}
