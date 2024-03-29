package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;

import java.util.List;

public interface Document extends Buildable {

    /*
     * Static factory methods
     */
    static Document document(FragmentOrOperation... operations) {
        Document document = getNewInstanceOf(Document.class);

        document.setOperations(asList(operations));

        return document;
    }

    /*
     * Getter/Setter
     */
    List<FragmentOrOperation> getOperations();

    void setOperations(List<FragmentOrOperation> operations);
}
