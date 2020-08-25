package io.smallrye.graphql.execution.batchloader;

import io.smallrye.graphql.schema.model.Operation;

/**
 * Helping with BatchLoaders
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceBatchLoaderHelper {

    private SourceBatchLoaderHelper() {
    }

    public static String getName(Operation operation) {
        return operation.getContainingType().getName() + "_" + operation.getName();
    }
}
