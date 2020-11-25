package io.smallrye.graphql.test.apps.generics.api;

import org.eclipse.microprofile.graphql.Name;

@Name("ClassWithoutGenericsWithNameAnnotationChanged")
public class ClassWithoutGenericsWithNameAnnotation {

    public String getName() {
        return "name";
    }

    public int getAge() {
        return 42;
    }
}
