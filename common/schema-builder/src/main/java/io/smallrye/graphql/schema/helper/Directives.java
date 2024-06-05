package io.smallrye.graphql.schema.helper;

import static java.util.stream.Collectors.toList;
import static org.jboss.jandex.AnnotationValue.Kind.ARRAY;
import static org.jboss.jandex.AnnotationValue.Kind.NESTED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import graphql.language.StringValue;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.requiresscopes.RequiresScopes;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.ScanningContext;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;

public class Directives {

    // Directives generated from application annotations that have a `@Directive` on them.
    // These directive types are expected to have the `className` field defined
    private final Map<DotName, DirectiveType> directiveTypes;

    // Other directive types - for example, directives from bean validation constraints.
    private final List<DirectiveType> directiveTypesOther;

    private static final Logger LOG = Logger.getLogger(Directives.class.getName());

    public Directives(List<DirectiveType> directiveTypes) {
        // not with streams/collector, so duplicate keys are allowed and overwritten
        this.directiveTypes = new HashMap<>();
        this.directiveTypesOther = new ArrayList<>();
        for (DirectiveType directiveType : directiveTypes) {
            if (directiveType.getClassName() != null) {
                this.directiveTypes.put(DotName.createSimple(directiveType.getClassName()), directiveType);
            } else {
                this.directiveTypesOther.add(directiveType);
            }
        }
    }

    public List<DirectiveInstance> buildDirectiveInstances(Annotations annotations, String directiveLocation,
            String referenceName) {
        // only build directive instances from `@Directive` annotations here (that means the `directiveTypes` map),
        // because `directiveTypesOther` directives get their instances added on-the-go by classes that extend `ModelCreator`
        return directiveTypes.keySet().stream()
                .flatMap(annotations::resolve)
                .map(this::toDirectiveInstance)
                .filter(directiveInstance -> {
                    if (!directiveInstance.getType().getLocations().contains(directiveLocation)) {
                        LOG.warnf(
                                "Directive instance: '%s' assigned to '%s' cannot be applied." +
                                        " The directive is allowed on locations '%s' but on '%s'",
                                directiveInstance.getType().getClassName(), referenceName,
                                directiveInstance.getType().getLocations(), directiveLocation);
                        return false;
                    }
                    return true;
                })
                .collect(toList());
    }

    private DirectiveInstance toDirectiveInstance(AnnotationInstance annotationInstance) {
        DirectiveInstance directiveInstance = new DirectiveInstance();
        DirectiveType directiveType = directiveTypes.get(annotationInstance.name());
        directiveInstance.setType(directiveType);

        for (AnnotationValue annotationValue : annotationInstance.values()) {
            String annotationValueName = getAnnotationValueName(directiveType, annotationValue.name());
            if (directiveType.getClassName().equals(Policy.class.getName()) ||
                    directiveType.getClassName().equals(RequiresScopes.class.getName())) {
                // For both of these directives, we need to process the annotation values as nested arrays of strings
                directiveInstance.setValue(annotationValueName, valueObjectNestedList(annotationValue));
            } else {
                if (directiveType.getClassName().equals(Key.class.getName()) && annotationValueName.equals("fields")) {
                    directiveInstance.setValue(annotationValueName,
                            new StringValue((String) valueObject(annotationValue.asNested().value())));
                } else {
                    directiveInstance.setValue(annotationValueName, valueObject(annotationValue));
                }
            }
        }

        return directiveInstance;
    }

    private String getAnnotationValueName(DirectiveType directiveType, String annotationName) {
        ClassInfo classInfo = ScanningContext.getIndex().getClassByName(directiveType.getClassName());

        Optional<MethodInfo> matchingMethod = classInfo.methods().stream()
                .filter(methodInfo -> methodInfo.name().equals(annotationName))
                .findFirst();
        if (matchingMethod.isPresent()) {
            // If the method name is modified when creating the directive type, we need to use the modified name
            // e.g. Link._import and Link._for
            MethodInfo method = matchingMethod.get();
            return TypeNameHelper.getMethodName(matchingMethod.get(),
                    Annotations.getAnnotationsForInterfaceField(method));
        }
        return annotationName;
    }

    private Object valueObject(AnnotationValue value) {
        if (value.kind() == ARRAY) {
            AnnotationValue[] annotationValues = (AnnotationValue[]) value.value();
            Object[] objects = new Object[annotationValues.length];
            for (int i = 0; i < annotationValues.length; i++) {
                objects[i] = valueObject(annotationValues[i]);
            }
            return objects;
        } else if (value.kind() == NESTED) {
            AnnotationInstance annotationInstance = (AnnotationInstance) value.value();
            Map<String, Object> values = new LinkedHashMap<>();
            if (annotationInstance != null) {
                for (AnnotationValue annotationValue : annotationInstance.values()) {
                    values.put(annotationValue.name(), valueObject(annotationValue));
                }
            }
            return values;
        }
        return value.value();
    }

    // This method is used specifically in cases where we wish to ignore upper nested objects (e.g. PolicyGroup)
    private List<List<Object>> valueObjectNestedList(AnnotationValue value) {
        valueObject(value);
        List<List<Object>> values = new ArrayList<>();
        for (AnnotationValue annotationValue : value.asArrayList()) {
            List<Object> nestedValues = new ArrayList<>();
            for (AnnotationValue nestedAnnotationValue : annotationValue.asNested().values().get(0).asArrayList()) {
                nestedValues.add(valueObject(nestedAnnotationValue));
            }
            values.add(nestedValues);
        }
        return values;
    }

    public Map<DotName, DirectiveType> getDirectiveTypes() {
        return directiveTypes;
    }
}
