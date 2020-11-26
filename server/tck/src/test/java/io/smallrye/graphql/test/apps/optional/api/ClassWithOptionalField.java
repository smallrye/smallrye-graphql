package io.smallrye.graphql.test.apps.optional.api;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalDouble;

import org.eclipse.microprofile.graphql.DateFormat;

import io.smallrye.graphql.api.Scalar;
import io.smallrye.graphql.api.ToScalar;

public class ClassWithOptionalField {

    @ToScalar(Scalar.Int.class)
    public Optional<Long> id;

    public Optional<String> maybe;

    @DateFormat("dd MMM yyyy")
    public Optional<LocalDate> maybeOneDay;

    @ToScalar(Scalar.String.class)
    public Optional<Mapped> mapped;

    public OptionalDouble amount;

}
