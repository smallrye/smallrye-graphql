package io.smallrye.graphql.client.model;

import static io.smallrye.graphql.client.model.Annotations.GRAPHQL_CLIENT_API;
import static io.smallrye.graphql.client.model.ScanningContext.getIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.client.model.helper.OperationModel;

/**
 * Builder class for generating client models from Jandex index.
 * It scans for classes annotated with {@link Annotations#GRAPHQL_CLIENT_API} and generates client models based on the annotated
 * methods.
 *
 * @author mskacelik
 */

public class ClientModelBuilder {
    private static final Logger LOG = Logger.getLogger(ClientModelBuilder.class.getName());

    /**
     * Builds the client models from the given Jandex index.
     *
     * @param index The Jandex index containing class information.
     * @return The generated client models.
     */
    public static ClientModels build(IndexView index) {
        ScanningContext.register(index);
        return new ClientModelBuilder().generateClientModels();
    }

    private ClientModelBuilder() {
    }

    /**
     * Generates the client models by scanning classes annotated with {@link Annotations#GRAPHQL_CLIENT_API}.
     *
     * @return The generated client models.
     */
    private ClientModels generateClientModels() {
        ClientModels clientModels = new ClientModels();
        Map<String, ClientModel> clientModelMap = new HashMap<>();

        // Get all the @GraphQLClientApi annotations
        Collection<AnnotationInstance> graphQLApiAnnotations = getIndex()
                .getAnnotations(GRAPHQL_CLIENT_API);

        graphQLApiAnnotations.forEach(graphQLApiAnnotation -> {
            ClientModel operationMap = new ClientModel();
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            List<MethodInfo> methods = getAllMethodsIncludingFromSuperClasses(apiClass);
            methods.stream().forEach(method -> {
                String query = new QueryBuilder(method).build();
                LOG.debugf("[%s] – Query created: %s", apiClass.name().toString(), query);
                operationMap.getOperationMap()
                        .putIfAbsent(OperationModel.of(method).getMethodKey(), query);
            });
            clientModelMap.put(
                    (graphQLApiAnnotation.value("configKey") == null) ? apiClass.name().toString()
                            : graphQLApiAnnotation.value("configKey").asString(),
                    operationMap);
        });
        clientModels.setClientModelMap(clientModelMap);
        return clientModels;
    }

    /**
     * Retrieves all methods, including those from superclasses (interfaces).
     *
     * @param classInfo The ClassInfo for which methods are retrieved.
     * @return The list of MethodInfo objects representing all methods.
     */
    private List<MethodInfo> getAllMethodsIncludingFromSuperClasses(ClassInfo classInfo) {
        List<MethodInfo> methods = new ArrayList<>();
        classInfo.methods().stream().filter(methodInfo -> !methodInfo.isSynthetic()).forEach(methods::add);
        List<DotName> interfaceNames = classInfo.interfaceNames();
        interfaceNames.forEach(interfaceName -> {
            List<MethodInfo> parentMethods = getAllMethodsIncludingFromSuperClasses(getIndex().getClassByName(interfaceName));
            methods.addAll(parentMethods);
        });

        return methods;
    }
}
