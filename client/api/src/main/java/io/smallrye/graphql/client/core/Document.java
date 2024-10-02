package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.DocumentFactory;

public interface Document extends Buildable {

    /*
     * Static factory methods
     */
    static Document document(FragmentOrOperation... operations) {
        Document document = getNewInstanceFromFactory(DocumentFactory.class);

        document.setOperations(asList(operations));

        return document;
    }

    /*
     * Getter/Setter
     */
    List<FragmentOrOperation> getOperations();

    void setOperations(List<FragmentOrOperation> operations);
}
