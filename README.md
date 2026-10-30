# Adoptium marketplace

This repo contains:

- adoptium-marketplace-schema
    - Schema definition for vendors to advertise their binaries
- adoptium-marketplace-client
    - Client library for reading a repository with vendor data
- adoptium-marketplace-server
    - Implementation of the adoptium marketplace API
- exampleRepositories
    - Examples of a vendor repository

# Build

Build with

```shell
./mvnw clean install
```

# Testing

Tests rely on the data inside the `exampleRepositories` directory in order for tests to pass they must be signed. If you wish to modify test assets they need to be re-signed once they have been modified. The procedure would be as follows:

- Generate test keys
    - Look in the `exampleRepositories/keys` directory for scripts that detail generating keys
- Re-sign assets
    - Run `SignTestAssets` in the `adoptium-marketplace-utils` project.

# Repository validation

## Extended OpenJDK versions

`openjdk_version_data` supports numeric version components after `patch` through
the optional `additional_version_numbers` array. For example, `25.0.5.0.2+1` is
represented as:

```json
{
  "major": 25,
  "minor": 0,
  "security": 5,
  "patch": 0,
  "additional_version_numbers": [2],
  "build": 1,
  "openjdk_version": "25.0.5.0.2+1"
}
```

When the array is absent, null, or empty, the components are extracted from
`openjdk_version`. A non-empty explicit array takes precedence. Versions without
additional components use an empty list, which is omitted when serialized.
Additional components are compared numerically, in order, before prerelease and
build information; a shorter matching sequence sorts before a longer one.

## Validating a repository

A repository can be validated using the `MarketplaceClient`. The client pulls a repository and validates its contents. For example:

```java
    String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
    // Public key string here
    "-----END PUBLIC KEY-----";
    String repoUrl = "http://localhost:8080/repo";

    try {
        MarketplaceClient client = MarketplaceClient.build(repoUrl, SignatureType.BASE64_ENCODED, publicKey);
        ReleaseList releaseList = client.readRepositoryData();
    
        System.out.println("Found: " + releaseList.getReleases().size() + " releases");
    } catch (Exception e) {
        System.err.println("Validation failed");
        e.printStackTrace();
    }
```

Note that in this example we have used the default `SignatureType.BASE64_ENCODED` which specifies that the signature files are
base64 encoded. If you require non-base64 encoded use `SignatureType.SIG`.

An example of running this can be seen in RepoTest class in the `adoptium-marketplace-client` module. To validate your repo using this test,
edit it to add your public key and repo location, then run with:

```
VALIDATE_REPO=true ../mvnw test -Dtest=RepoTest#validateRepo
```

from inside the `adoptium-marketplace-client` directory.
