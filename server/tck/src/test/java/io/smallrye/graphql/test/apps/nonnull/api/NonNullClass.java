package io.smallrye.graphql.test.apps.nonnull.api;

import java.util.Optional;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Type;

import io.smallrye.graphql.api.AllNonNull;
import io.smallrye.graphql.api.Nullable;

@AllNonNull
@Type
@Input
public class NonNullClass {

    public String nonNullString = "";

    public Optional<String> optionalString = Optional.empty();

    @Nullable
    public String nullableString = null;

}
