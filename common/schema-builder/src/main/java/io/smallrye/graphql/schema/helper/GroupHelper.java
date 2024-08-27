package io.smallrye.graphql.schema.helper;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.Group;

/**
 * Helping with Group creation
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GroupHelper {

    private GroupHelper() {
    }

    public static Optional<Group> getGroup(AnnotationInstance graphQLApiAnnotation) {
        Optional<String> name = getName(graphQLApiAnnotation);
        if (name.isPresent()) {
            Optional<String> description = getDescription(graphQLApiAnnotation);
            Group group = new Group();
            group.setName(name.get());
            group.setDescription(description.orElse(null));
            return Optional.of(group);
        }
        return Optional.empty();
    }

    /**
     * This gets the root name (by default 'root).
     * This will allow grouping root queries under a logical name.
     *
     * @param graphQLApiAnnotation
     * @return
     */
    private static Optional<String> getName(AnnotationInstance graphQLApiAnnotation) {
        // Get the name
        AnnotationValue value = graphQLApiAnnotation.value();
        if (value != null && value.asString() != null && !value.asString().isEmpty()) {
            return Optional.of(value.asString());
        } else {
            // Try the Name annotation
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            AnnotationInstance nameAnnotation = apiClass.classAnnotation(Annotations.NAME);
            if (nameAnnotation != null && nameAnnotation.value() != null && nameAnnotation.value().asString() != null
                    && !nameAnnotation.value().asString().isEmpty()) {
                return Optional.of(nameAnnotation.value().asString());
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
        if (apiClass.annotationsMap().containsKey(Annotations.DESCRIPTION)) {
            List<AnnotationInstance> descriptionAnnotations = apiClass.annotationsMap().get(Annotations.DESCRIPTION);
            if (descriptionAnnotations != null && !descriptionAnnotations.isEmpty()) {
                for (AnnotationInstance descriptionAnnotation : descriptionAnnotations) {
                    if (descriptionAnnotation.target().equals(graphQLApiAnnotation.target())){
                        AnnotationValue value = descriptionAnnotation.value();
                        if (value != null && value.asString() != null && !value.asString().isEmpty()) {
                            return Optional.of(value.asString());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}
