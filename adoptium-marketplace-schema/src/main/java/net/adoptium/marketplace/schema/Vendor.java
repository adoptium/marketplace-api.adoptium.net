package net.adoptium.marketplace.schema;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.stream.Stream;

@Schema(
        type = SchemaType.STRING,
        enumeration = {
                "adoptium", "alibaba", "ibm", "microsoft", "azul"
        },

        description = """
                The vendor of the OpenJDK distribution.
                
                The following vendors are no longer receiving updates or new releases for their OpenJDK distributions:
                
                <ul>
                    <li><b>huawei</b> - Huawei OpenJDK</li>
                </ul>
                """)
public enum Vendor {
    adoptium, alibaba, ibm, microsoft, azul, huawei;

    /**
     * List of vendors that are no longer supported. These will no longer receive updates or new releases.
     * <p>
     * This list can be configured via the environment variable
     * {@link #NO_LONGER_SUPPORTED_VENDORS_PROPERTY}.
     */
    public static final List<Vendor> NO_LONGER_SUPPORTED_VENDORS = getNoLongerSupportedVendors();

    private static final String NO_LONGER_SUPPORTED_VENDORS_PROPERTY = "NO_LONGER_SUPPORTED_VENDORS";

    private static List<Vendor> getNoLongerSupportedVendors() {

        String outOfSupport = System.getenv(NO_LONGER_SUPPORTED_VENDORS_PROPERTY);

        if (outOfSupport != null && !outOfSupport.isEmpty()) {
            return Stream.of(outOfSupport.split(","))
                    .map(String::trim)
                    .map(Vendor::valueOf)
                    .toList();
        } else {
            return List.of(Vendor.huawei);
        }
    }
}
