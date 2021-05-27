package io.smallrye.graphql.test.apps.batch.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class BatchApi {

    // Normal List Query
    @Query
    public List<BatchPojo> batchPojos() {
        List<BatchPojo> l = new ArrayList<>();
        l.add(new BatchPojo(1));
        l.add(new BatchPojo(2));
        l.add(new BatchPojo(3));
        l.add(new BatchPojo(4));

        return l;
    }

    // Normal Source
    public String greeting(@Source BatchPojo batchPojo) {
        return "hello";
    }

    // Normal Batch Source
    public List<UUID> uuid(@Source List<BatchPojo> batchPojos) {
        List<UUID> uuids = new ArrayList<>();
        for (BatchPojo batchPojo : batchPojos) {
            uuids.add(UUID.fromString("88aaea3a-a48c-46de-a675-d6f2a65a9b2" + batchPojo.id));
        }
        return uuids;
    }

    // Normal Source on interface
    public String interfaceGreeting(@Source BatchInterface batchPojo) {
        return "interface-hello";
    }

    // Normal Batch Source on interface
    public List<String> interfaceId(@Source List<BatchInterface> batchPojos) {
        List<String> uuids = new ArrayList<>();
        for (BatchInterface batchPojo : batchPojos) {
            uuids.add(("interfaceId-" + ((BatchPojo) batchPojo).id));
        }
        return uuids;
    }

    // Runtime Exception Source
    public String runtime(@Source BatchPojo batchPojo) {
        throw new RuntimeException("Some runtime exception");
    }

    // Runtime Exception Batch Source
    public List<String> runtimes(@Source List<BatchPojo> batchPojos) {
        throw new RuntimeException("Some runtimes exception");
    }

    // Business Exception Source
    public String business(@Source BatchPojo batchPojo) throws BusinessException {
        throw new BusinessException("Some business exception");
    }

    // Business Exception Batch Source
    public List<String> businesses(@Source List<BatchPojo> batchPojos) throws BusinessException {
        throw new BusinessException("Some businesses exception");
    }

}
