package io.smallrye.graphql.schema.helper;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.Namespace;

public class NamespaceHelper {
    private NamespaceHelper() {
    }

    public static Optional<Namespace> getNamespace(AnnotationInstance graphQLApiAnnotation) {
        Optional<List<String>> names = getNames(graphQLApiAnnotation);
        if (names.isPresent()) {
            Optional<String> description = getDescription(graphQLApiAnnotation);
            Namespace group = new Namespace();
            group.setNames(names.get());
            group.setDescription(description.orElse(null));
            return Optional.of(group);
        }
        return Optional.empty();
    }

    /**
     * This gets the namespaces.
     * This will allow grouping root queries under a logical name.
     *
     * @param graphQLApiAnnotation annotation on the class
     * @return list of namespaces
     */
    private static Optional<List<String>> getNames(AnnotationInstance graphQLApiAnnotation) {
        ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
        if (apiClass.hasDeclaredAnnotation(Annotations.NAMESPACE)) {
            String[] namespaces = apiClass.declaredAnnotation(Annotations.NAMESPACE).value().asStringArray();
            if (namespaces.length > 0) {
                return Optional.of(List.of(namespaces));
            }
        }

        if (apiClass.hasDeclaredAnnotation(Annotations.NAME)) {
            String value = apiClass.declaredAnnotation(Annotations.NAME).value().asString();
            if (!value.isEmpty()) {
                return Optional.of(List.of(value));
            }
        }
        return Optional.empty();
    }

    /**
     * Get the description on a class type
     *
     * @param graphQLApiAnnotation annotation on the class
     * @return the optional description
     */
    private static Optional<String> getDescription(AnnotationInstance graphQLApiAnnotation) {
        ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
        if (apiClass.hasDeclaredAnnotation(Annotations.DESCRIPTION)) {
            String value = apiClass.declaredAnnotation(Annotations.DESCRIPTION).value().asString();
            if (!value.isEmpty()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
