package io.smallrye.graphql.schema.helper.class_nonnull;

import java.util.Optional;

import io.smallrye.graphql.api.AllNonNull;
import io.smallrye.graphql.api.Nullable;

@AllNonNull
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
