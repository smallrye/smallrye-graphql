package io.smallrye.graphql.test.apps.batch.api;

public class BatchPojo implements BatchInterface {

    public int id;

    public BatchPojo() {
    }

    public BatchPojo(int id) {
        this.id = id;
    }

    public String getSimpleField() {
        return "Some String";
    }
}
