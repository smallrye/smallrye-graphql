package io.smallrye.graphql.schema.helper.package_nonnull;

import java.util.Optional;

import io.smallrye.graphql.api.Nullable;

public class PackageNonNullTestApi {

    public String string() {
        return "";
    }

    public Optional<String> optionalString() {
        return Optional.empty();
    }

    @Nullable
    public String nullableString() {
        return "";
    }

}
