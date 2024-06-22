package io.smallrye.graphql.client.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a model object used by Quarkus during both build-time and runtime.
 * This class encapsulates all of the operation queries for a single client API.
 *
 * @author mskacelik
 */
public class ClientModel {

    private Map<MethodKey, String> operationQueryMap;

    public ClientModel() {
        operationQueryMap = new HashMap<>();
    };

    // bytecode recording
    public Map<MethodKey, String> getOperationMap() {
        return operationQueryMap;
    }

    public void setOperationMap(Map<MethodKey, String> operationQueryMap) {
        this.operationQueryMap = operationQueryMap;
    }

    // for testing purposes...
    @Override
    public String toString() {
        return "ClientModel{" +
                "operationQueryMap=" + operationQueryMap +
                '}';
    }
}