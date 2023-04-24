package io.smallrye.graphql.schema.helper;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.creator.ReferenceCreator;
import io.smallrye.graphql.schema.creator.WrapperCreator;
import io.smallrye.graphql.schema.model.AdaptWith;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Scalars;

/**
 * Helping with object adapters.
 * This adds support for Adapting one type to another.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AdaptWithHelper {

    private AdaptWithHelper() {
    }

    public static boolean shouldCreateTypeInSchema(Annotations annotations) {
        return !hasAnyAdaptWithAnnotations(annotations);
    }

    /**
     * Get the adaptWith for a certain field (if any)
     *
     * @param direction
     * @param referenceCreator
     * @param field
     * @param annotations the annotations
     * @return Potentially an AdaptWith model
     */
    public static Optional<AdaptWith> getAdaptWith(Direction direction, ReferenceCreator referenceCreator, Field field,
            Annotations annotations) {
        return getAdaptWith(direction, referenceCreator, field.getReference(), annotations);
    }

    /**
     * Get the adapt with for a certain reference.
     *
     * @param direction
     * @param referenceCreator
     * @param r
     * @param annotations the annotations
     * @param adapterType
     * @return Potentially a AdaptWith model
     */
    public static Optional<AdaptWith> getAdaptWith(Direction direction, ReferenceCreator referenceCreator, Reference r,
            Annotations annotations, AdapterType adapterType) {
        if (r.isAdaptingWith()) {
            return Optional.of(r.getAdaptWith());
        } else if (adapterType != null) {
            Type type = adapterType.type;
            AdaptWith adaptWith = adapterType.adaptWith;
            if (type.kind().equals(Type.Kind.CLASS)) {
                ClassInfo classInfo = ScanningContext.getIndex().getClassByName(type.name());
                List<Type> interfaceTypes = classInfo.interfaceTypes();

                // Look for supported interfaces
                for (Type interfaceType : interfaceTypes) {
                    if (interfaceType.name().toString().equals(adaptWith.getAdapterInterface())
                            && interfaceType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
                        ParameterizedType adapterInterface = interfaceType.asParameterizedType();
                        List<Type> arguments = adapterInterface.arguments();
                        if (arguments.size() == 2) {
                            Type from = arguments.get(0);
                            Type to = arguments.get(1);
                            adaptWith.setAdapterClass(type.name().toString());
                            r.setWrapper(WrapperCreator.createWrapper(from).orElse(null));
                            adaptWith.setFromReference(r);

                            if (Scalars.isScalar(to.name().toString())) {
                                adaptWith.setToReference(Scalars.getScalar(to.name().toString()));
                            } else {
                                Annotations annotationsAplicableToMe = annotations.removeAnnotations(Annotations.ADAPT_WITH,
                                        Annotations.JAKARTA_JSONB_TYPE_ADAPTER, Annotations.JAVAX_JSONB_TYPE_ADAPTER);

                                // Remove the adaption annotation, as this is the type being adapted to
                                Reference toRef = referenceCreator.createReferenceForAdapter(to,
                                        annotationsAplicableToMe, direction);
                                toRef.setWrapper(WrapperCreator.createWrapper(to).orElse(null));

                                adaptWith.setToReference(toRef);
                            }

                            return Optional.of(adaptWith);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get the adapt with for a certain reference.
     *
     * @param direction
     * @param referenceCreator
     * @param r
     * @param annotations the annotations
     * @return Potentially a AdaptWith model
     */
    public static Optional<AdaptWith> getAdaptWith(Direction direction, ReferenceCreator referenceCreator, Reference r,
            Annotations annotations) {
        AdapterType adapterType = getAdapterType(annotations);
        return getAdaptWith(direction, referenceCreator, r, annotations, adapterType);
    }

    private static AdapterType getAdapterType(Annotations annotations) {
        if (annotations != null) {
            // First check out own annotation
            if (annotations.containsOneOfTheseAnnotations(Annotations.ADAPT_WITH)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.ADAPT_WITH);
                if (annotationValue != null) {
                    AdaptWith adaptWith = new AdaptWith(Classes.ADAPTER.toString(),
                            "from", "to");
                    Type type = annotationValue.asClass();
                    return new AdapterType(type, adaptWith);
                }
            }

            // Also add support for JsonB
            if (annotations.containsOneOfTheseAnnotations(Annotations.JAKARTA_JSONB_TYPE_ADAPTER)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.JAKARTA_JSONB_TYPE_ADAPTER);
                if (annotationValue != null) {
                    AdaptWith adaptWith = new AdaptWith(Classes.JAKARTA_JSONB_ADAPTER.toString(),
                            "adaptFromJson", "adaptToJson");
                    Type type = annotationValue.asClass();
                    return new AdapterType(type, adaptWith);
                }
            }
            if (annotations.containsOneOfTheseAnnotations(Annotations.JAVAX_JSONB_TYPE_ADAPTER)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.JAVAX_JSONB_TYPE_ADAPTER);
                if (annotationValue != null) {
                    AdaptWith adaptWith = new AdaptWith(Classes.JAVAX_JSONB_ADAPTER.toString(),
                            "adaptFromJson", "adaptToJson");
                    Type type = annotationValue.asClass();
                    return new AdapterType(type, adaptWith);
                }
            }

            // TODO: add support for Jackson ?
        }
        return null;
    }

    private static boolean hasAnyAdaptWithAnnotations(Annotations annotations) {
        AdapterType adapterType = getAdapterType(annotations);
        return adapterType != null && adapterType.type != null;
    }

    private static class AdapterType {
        Type type;
        AdaptWith adaptWith;

        public AdapterType(Type type, AdaptWith adaptWith) {
            this.type = type;
            this.adaptWith = adaptWith;
        }
    }
}
