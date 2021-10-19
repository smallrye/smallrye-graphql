package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.core.FragmentOrOperation;

public class DocumentImpl extends AbstractDocument {

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        for (FragmentOrOperation operation : this.getOperations()) {
            builder.append(operation.build());
        }
        return builder.toString();
    }

}
