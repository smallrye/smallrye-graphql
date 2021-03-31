package io.smallrye.graphql.client.dynamic.core;

public class EnumImpl extends AbstractEnum {

    @Override
    public String build() {
        return this.getValue();
    }

}
