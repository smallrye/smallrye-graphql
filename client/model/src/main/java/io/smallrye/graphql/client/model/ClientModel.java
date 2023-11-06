package io.smallrye.graphql.client.model;

import java.util.HashMap;
import java.util.Map;

public class ClientModel {

    private Map<MethodKey, String> operationQueryMap;

    public ClientModel() {
        operationQueryMap = new HashMap<>();
    };

    public Map<MethodKey, String> getOperationMap() {
        return operationQueryMap;
    }

    public void setOperationMap(Map<MethodKey, String> operationQueryMap) {
        this.operationQueryMap = operationQueryMap;
    }

    @Override
    public String toString() {
        return "ClientModel{" +
                "operationQueryMap=" + operationQueryMap +
                '}';
    }
}