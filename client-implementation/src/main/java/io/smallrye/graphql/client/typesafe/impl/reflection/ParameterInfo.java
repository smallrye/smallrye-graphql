package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.reflect.Parameter;

import org.eclipse.microprofile.graphql.Name;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ParameterInfo {
    private final MethodInfo method;
    private final Parameter parameter;
    @Getter
    private final TypeInfo type;
    @Getter
    private final Object value;

    @Override
    public String toString() {
        return "parameter '" + parameter.getName() + "' in " + method;
    }

    public String getName() {
        if (parameter.isAnnotationPresent(Name.class))
            return parameter.getAnnotation(Name.class).value();
        if (!parameter.isNamePresent())
            throw new GraphQlClientException("compile with -parameters to add the parameter names to the class file");
        return parameter.getName();
    }
}
