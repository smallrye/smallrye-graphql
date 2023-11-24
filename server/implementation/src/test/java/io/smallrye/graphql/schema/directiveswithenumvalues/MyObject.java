package io.smallrye.graphql.schema.directiveswithenumvalues;

import java.util.Objects;

import org.eclipse.microprofile.graphql.NonNull;

public class MyObject {

    @NonNull
    @MyEnumValueDirective(MyEnum.SOME)
    String name;

    public MyObject(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

}
