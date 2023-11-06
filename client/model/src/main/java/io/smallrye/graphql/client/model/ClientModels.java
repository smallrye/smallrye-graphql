package io.smallrye.graphql.client.model;

import java.util.HashMap;
import java.util.Map;

public class ClientModels {
    private Map<String, ClientModel> clientModelMap;

    public ClientModel getClientModelByConfigKey(String configKey) {
        return clientModelMap.get(configKey);
    }

    // bytecode recording
    public ClientModels() {
        clientModelMap = new HashMap<>();
    }

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
