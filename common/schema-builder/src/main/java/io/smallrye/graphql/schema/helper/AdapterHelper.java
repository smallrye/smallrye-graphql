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
import io.smallrye.graphql.schema.model.Adapter;

/**
 * Helping with object adapters.
 * This adds support for Adapting one type to another.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AdapterHelper {

    private AdapterHelper() {
    }

    /**
     * Get the adapter for a certain field.
     * 
     * @param annotations the annotations
     * @return Potentially a Adapter info
     */

    public static Optional<Adapter> getAdapter(Annotations annotations) {

        AdapterType adapterType = getAdapterType(annotations);

        if (adapterType != null) {

            Type type = adapterType.type;
            System.err.println(">>>>> type " + type);
            Adapter adapter = adapterType.adapter;
            System.err.println(">>>>> adapter " + adapter);
            if (type.kind().equals(Type.Kind.CLASS)) {
                ClassInfo classInfo = ScanningContext.getIndex().getClassByName(type.name());
                List<Type> interfaceTypes = classInfo.interfaceTypes();

                // Look for supported intefaces
                for (Type interfaceType : interfaceTypes) {
                    System.err.println(">>>>> interfaceType.name().toString() " + interfaceType.name().toString());
                    System.err.println(">>>>> adapter.getAdapterInterface() " + adapter.getAdapterInterface());
                    if (interfaceType.name().toString().equals(adapter.getAdapterInterface())
                            && interfaceType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
                        ParameterizedType adapterInterface = interfaceType.asParameterizedType();
                        List<Type> arguments = adapterInterface.arguments();
                        if (arguments.size() == 2) {
                            Type from = arguments.get(0);
                            Type to = arguments.get(1);

                            adapter.setAdapterClass(type.name().toString());
                            adapter.setFromClass(from.name().toString());
                            adapter.setToClass(to.name().toString());

                            return Optional.of(adapter);
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static AdapterType getAdapterType(Annotations annotations) {
        if (annotations != null) {
            // First check out own annotation
            if (annotations.containsOneOfTheseAnnotations(Annotations.ADAPT_WITH)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.ADAPT_WITH);
                if (annotationValue != null) {
                    Adapter adapter = new Adapter(Classes.ADAPTER.toString(),
                            "from", "to");
                    Type type = annotationValue.asClass();
                    return new AdapterType(type, adapter);
                }
            }

            // Also add support for JsonB
            if (annotations.containsOneOfTheseAnnotations(Annotations.JSONB_TYPE_ADAPTER)) {
                AnnotationValue annotationValue = annotations.getAnnotationValue(Annotations.JSONB_TYPE_ADAPTER);
                if (annotationValue != null) {
                    Adapter adapter = new Adapter(Classes.JSONB_ADAPTER.toString(),
                            "adaptFromJson", "adaptToJson");
                    Type type = annotationValue.asClass();
                    return new AdapterType(type, adapter);
                }
            }
        }
        return null;
    }

    private static class AdapterType {
        Type type;
        Adapter adapter;

        public AdapterType(Type type, Adapter adapter) {
            this.type = type;
            this.adapter = adapter;
        }
    }
}
