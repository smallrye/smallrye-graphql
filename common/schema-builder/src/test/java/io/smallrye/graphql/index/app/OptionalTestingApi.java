package io.smallrye.graphql.index.app;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class OptionalTestingApi {

    private final LocalDate oneday = LocalDate.of(2020, 12, 25);

    @Query
    public ClassWithOptionalField classWithOptionalField() {
        ClassWithOptionalField classWithOptionalField = new ClassWithOptionalField();
        classWithOptionalField.maybe = Optional.of("Hello there");
        classWithOptionalField.maybeOneDay = Optional.of(oneday);
        classWithOptionalField.id = Optional.of(13L);
        classWithOptionalField.mapped = Optional.of(new Mapped("I am a mapped object"));
        classWithOptionalField.amount = OptionalDouble.of(1.23);
        return classWithOptionalField;
    }

    @Query
    public ClassWithOptionalField classWithEmptyField() {
        ClassWithOptionalField classWithOptionalField = new ClassWithOptionalField();
        classWithOptionalField.maybe = Optional.empty();
        classWithOptionalField.maybeOneDay = Optional.empty();
        classWithOptionalField.id = Optional.empty();
        classWithOptionalField.mapped = Optional.empty();
        classWithOptionalField.amount = OptionalDouble.empty();
        return classWithOptionalField;
    }

    @Query
    public Optional<String> optionalString() {
        return Optional.of("Hello there");
    }

    @Query
    public Optional<String> emptyString() {
        return Optional.empty();
    }

    @Query
    public String optionalStringParam(Optional<String> echo) {
        return echo.orElse(null);
    }

    @Query
    public LocalDate optionalDateParam(@DateFormat("dd MMM yyyy") Optional<LocalDate> echo) {
        return echo.orElse(null);
    }

    @Query
    public OptionalInt optionalInt() {
        return OptionalInt.of(7);
    }

    @Query
    public OptionalInt emptyInt() {
        return OptionalInt.empty();
    }
}