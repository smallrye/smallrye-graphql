package io.smallrye.graphql.test.apps.generics.api;

import static java.time.ZoneOffset.UTC;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClassWithGenericArrayAttributeResolvedFromEnclosingClass<V> {
    private final Date date = Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(UTC).toInstant());
    private V v;

    public ClassWithGenericArrayAttributeResolvedFromEnclosingClass() {

    }

    public ClassWithGenericArrayAttributeResolvedFromEnclosingClass(V v) {
        this.v = v;
    }

    public ClassWithTwoGenericsParams<V, Date>[] getParam1() {
        List<ClassWithTwoGenericsParams<V, Date>> classWithTwoGenericsParamses = new ArrayList<>();
        classWithTwoGenericsParamses.add(new ClassWithTwoGenericsParams<>(v, date));

        return classWithTwoGenericsParamses.toArray(new ClassWithTwoGenericsParams[] {});
    }

    public String getName() {
        return "name";
    }

    public void setV(V v) {
        this.v = v;
    }
}
