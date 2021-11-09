package io.smallrye.graphql.client.impl.core;

import java.util.List;

import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.FragmentOrOperation;

public abstract class AbstractDocument implements Document {
    private List<FragmentOrOperation> operations;

    public AbstractDocument() {
    }

    public List<FragmentOrOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<FragmentOrOperation> operations) {
        this.operations = operations;
    }
}
