package io.smallrye.graphql.index.generic;

public class Some implements Attribute<Long> {
    Long some;

    public Some() {
    }

    public Some(Long some) {
        this.some = some;
    }

    @Override
    public Long getValue() {
        return some;
    }

    @Override
    public void setValue(Long value) {
        this.some = value;
    }
}
