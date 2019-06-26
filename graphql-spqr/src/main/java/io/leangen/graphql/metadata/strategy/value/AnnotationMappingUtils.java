package io.leangen.graphql.metadata.strategy.value;

import java.lang.reflect.Method;

import org.eclipse.microprofile.graphql.InputField;

import io.leangen.graphql.util.Utils;

public class AnnotationMappingUtils {

    public static String inputFieldName(Method method) {
        if (method.isAnnotationPresent(InputField.class)) {
            return Utils.coalesce(method.getAnnotation(InputField.class).value(), method.getName());
        }
        return method.getName();
    }

    public static String inputFieldDescription(Method method) {
        return method.isAnnotationPresent(InputField.class) ? method.getAnnotation(InputField.class).description() : "";
    }
}
