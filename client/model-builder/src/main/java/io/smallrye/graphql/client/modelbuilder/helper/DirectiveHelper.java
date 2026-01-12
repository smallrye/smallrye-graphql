/**
 * Helper class for resolving GraphQL directives from annotated elements using Jandex.
 */
package io.smallrye.graphql.client.modelbuilder.helper;

import static io.smallrye.graphql.client.modelbuilder.Annotations.DIRECTIVE;
import static io.smallrye.graphql.client.modelbuilder.Annotations.REPEATABLE;
import static io.smallrye.graphql.client.modelbuilder.ScanningContext.getIndex;
import static java.util.Arrays.stream;

import java.util.Optional;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;

/**
 * Utility methods for resolving GraphQL directives from annotated elements.
 *
 * @author mskacelik
 */
public class DirectiveHelper {

    /**
     * Resolves GraphQL directives from a stream of annotation instances based on the given directive location and target kind.
     *
     * @param annotationInstances The stream of annotation instances to filter.
     * @param directiveLocation The GraphQL directive location.
     * @param targetKind The target kind of the annotation.
     * @return A stream of resolved annotation instances that match the specified criteria.
     */
    public static Stream<AnnotationInstance> resolveDirectives(Stream<AnnotationInstance> annotationInstances,
            String directiveLocation,
            AnnotationTarget.Kind targetKind) {
        return resolveDirectives(annotationInstances
                .filter(annotation -> annotation.target().kind() == targetKind), directiveLocation);
    }

    /**
     * Resolves GraphQL directives from a stream of annotation instances based on the given directive location.
     *
     * @param annotationInstances The stream of annotation instances to filter.
     * @param directiveLocation The GraphQL directive location.
     * @return A stream of resolved annotation instances that match the specified criteria.
     */
    public static Stream<AnnotationInstance> resolveDirectives(Stream<AnnotationInstance> annotationInstances,
            String directiveLocation) {
        return annotationInstances
                .flatMap(annotationInstance -> {
                    ClassInfo scannedAnnotation = getIndex().getClassByName(annotationInstance.name());
                    if (scannedAnnotation != null) {
                        if (scannedAnnotation.hasAnnotation(DIRECTIVE)) {
                            return Stream.of(annotationInstance);
                        }
                        Optional<AnnotationInstance> repeatableAnnotation = getIndex()
                                .getAnnotations(REPEATABLE).stream()
                                .filter(annotation -> annotation.target().hasAnnotation(DIRECTIVE)
                                        && annotation.value().asClass().name().equals(annotationInstance.name()))
                                .findFirst();
                        if (repeatableAnnotation.isPresent()) {
                            return Stream.of(annotationInstance.value().asNestedArray());
                        }
                    }
                    return Stream.empty();
                })
                .filter(annotation -> directiveFilter(annotation, directiveLocation));

    }

    /**
     * Checks if a given annotation instance matches the specified GraphQL directive location.
     *
     * @param annotation The annotation instance to check.
     * @param directiveLocation The GraphQL directive location to match.
     * @return {@code true} if the annotation matches the directive location, {@code false} otherwise.
     */
    private static boolean directiveFilter(AnnotationInstance annotation, String directiveLocation) {
        return stream(
                getIndex().getClassByName(annotation.name()).annotation(DIRECTIVE).value("on").asEnumArray())
                .anyMatch(directiveLocation::equals);
    }
}
