package io.smallrye.graphql.schema.helper;

import static org.jboss.jandex.AnnotationValue.Kind.ARRAY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;

public class Directives {
    private final Map<DotName, DirectiveType> directiveTypes;

    public Directives(List<DirectiveType> directiveTypes) {
        // not with streams/collector, so duplicate keys are allowed and overwritten
        this.directiveTypes = new HashMap<>();
        for (DirectiveType directiveType : directiveTypes) {
            this.directiveTypes.put(DotName.createSimple(directiveType.getClassName()), directiveType);
        }
    }

    public List<DirectiveInstance> buildDirectiveInstances(Function<DotName, AnnotationInstance> getAnnotation) {
        List<DirectiveInstance> result = null;
        for (DotName directiveTypeName : directiveTypes.keySet()) {
            AnnotationInstance annotationInstance = getAnnotation.apply(directiveTypeName);
            if (annotationInstance == null) {
                continue;
            }
            if (result == null) {
                result = new ArrayList<>();
            }
            result.add(toDirectiveInstance(annotationInstance));
        }
        return result;
    }

    private DirectiveInstance toDirectiveInstance(AnnotationInstance annotationInstance) {
        DirectiveInstance directiveInstance = new DirectiveInstance();
        directiveInstance.setType(directiveTypes.get(annotationInstance.name()));
        for (AnnotationValue annotationValue : annotationInstance.values()) {
            directiveInstance.setValue(annotationValue.name(), valueObject(annotationValue));
        }
        return directiveInstance;
    }

    private Object valueObject(AnnotationValue annotationValue) {
        if (annotationValue.kind() == ARRAY) {
            AnnotationValue[] values = (AnnotationValue[]) annotationValue.value();
            Object[] objects = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                objects[i] = valueObject(values[i]);
            }
            return objects;
        }
        return annotationValue.value();
    }
}
