package io.smallrye.graphql.schema.test_generics;

import org.eclipse.microprofile.graphql.Name;

@Name("ClassWithoutGenericsWithNameAnnotationChanged")
public class ClassWithoutGenericsWithNameAnnotation {

    public String getName() {
        return null;
    }

    public int getAge() {
        return 42;
    }
}
