package io.smallrye.graphql.client.model;

import static io.smallrye.graphql.client.model.helper.OperationModel.of;
import static java.util.stream.Collectors.joining;

import org.jboss.jandex.MethodInfo;

import io.smallrye.graphql.client.model.helper.DirectiveInstance;
import io.smallrye.graphql.client.model.helper.OperationModel;

/**
 * A utility class for building GraphQL queries based on a given {@link MethodInfo} which will be scanned thanks
 * to Jandex during build-time.
 *
 * @author mskacelik
 */
public class QueryBuilder {
    private final OperationModel method;

    /**
     * Constructs a new {@code QueryBuilder} instance for the specified {@link MethodInfo}.
     *
     * @param method The {@link MethodInfo} representing the GraphQL operation.
     */
    public QueryBuilder(MethodInfo method) {
        this.method = of(method);
    }

    /**
     * Builds and returns the GraphQL query string.
     *
     * @return The constructed GraphQL query string.
     */
    public String build() {
        StringBuilder request = new StringBuilder(method.getOperationTypeAsString());
        request.append(" ");
        request.append(method.getOperationName());
        if (method.hasValueParameters()) {
            request.append(method.valueParameters().stream().map(method::declare).collect(joining(", ", "(", ")")));
        }

        method.getNamespaces().forEach(namespace -> request.append(" { ").append(namespace));

        if (method.isSingle()) {
            request.append(" { ");
            request.append(method.getName());
            if (method.hasRootParameters()) {
                request.append(method.rootParameters().stream()
                        .map(method::bind)
                        .collect(joining(", ", "(", ")")));
            }

            if (method.hasDirectives()) {
                request.append(method.getDirectives().stream().map(DirectiveInstance::buildDirective).collect(joining()));
            }
        }

        request.append(method.fields(method.getReturnType()));

        if (method.isSingle()) {
            request.append(" }");
        }

        request.append(" }".repeat(method.getNamespaces().size()));

        return request.toString();
    }
}
