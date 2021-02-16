package io.smallrye.graphql.client.dynamic.core;

import java.util.List;

import io.smallrye.graphql.client.mpapi.core.Document;
import io.smallrye.graphql.client.mpapi.core.Operation;

public abstract class AbstractDocument implements Document {
    private List<Operation> operations;

    public AbstractDocument() {
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }
}
