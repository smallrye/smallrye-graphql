package io.smallrye.graphql.schema.helper.class_nonnull;

import java.util.Optional;

import io.smallrye.graphql.api.DefaultNonNull;
import io.smallrye.graphql.api.Nullable;

@DefaultNonNull
public class ClassNonNullTestApi {

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
