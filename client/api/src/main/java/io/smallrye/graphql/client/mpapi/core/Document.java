package io.smallrye.graphql.client.mpapi.core;

import static io.smallrye.graphql.client.mpapi.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;

import java.util.List;

public interface Document extends Buildable {

    /*
     * Static factory methods
     */
    static Document document(Operation... operations) {
        Document document = getNewInstanceOf(Document.class);

        document.setOperations(asList(operations));

        return document;
    }

    /*
     * Getter/Setter
     */
    List<Operation> getOperations();

    void setOperations(List<Operation> operations);
}
