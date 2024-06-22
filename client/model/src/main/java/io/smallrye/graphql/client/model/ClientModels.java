package io.smallrye.graphql.client.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an object for all `ClientModel` (based on their configuration keys)
 * instances within Quarkus application.
 *
 * @author mskacelik
 */

public class ClientModels {
    private Map<String, ClientModel> clientModelMap;

    public ClientModel getClientModelByConfigKey(String configKey) {
        return clientModelMap.get(configKey);
    }

    public ClientModels() {
        clientModelMap = new HashMap<>();
    }

    // bytecode recording
    public void setClientModelMap(Map<String, ClientModel> clientModelMap) {
        this.clientModelMap = clientModelMap;
    }

    public Map<String, ClientModel> getClientModelMap() {
        return clientModelMap;
    }

    // for testing purposes...
    @Override
    public String toString() {
        return "ClientModels{" +
                "operationMap=" + clientModelMap.toString() +
                '}';
    }
}
