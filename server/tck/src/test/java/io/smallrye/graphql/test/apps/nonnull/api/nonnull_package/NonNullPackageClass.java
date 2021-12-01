package io.smallrye.graphql.test.apps.nonnull.api.nonnull_package;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Type;

import io.smallrye.graphql.api.Nullable;

@Type
@Input
public class NonNullPackageClass {

    public String nonNullString = "";

    public Optional<String> optionalString = Optional.empty();

    @Nullable
    public String nullableString = null;

    public List<String> strings = Collections.emptyList();

}
