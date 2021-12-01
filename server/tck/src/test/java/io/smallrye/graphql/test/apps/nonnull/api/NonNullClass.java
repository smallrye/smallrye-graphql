package io.smallrye.graphql.test.apps.nonnull.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Type;

import io.smallrye.graphql.api.DefaultNonNull;
import io.smallrye.graphql.api.Nullable;

@DefaultNonNull
@Type
@Input
public class NonNullClass {

    public String nonNullString = "";

    public Optional<String> optionalString = Optional.empty();

    @Nullable
    public String nullableString = null;

    public List<String> strings = Collections.emptyList();

}
