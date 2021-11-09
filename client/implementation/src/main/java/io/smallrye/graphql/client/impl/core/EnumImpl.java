package io.smallrye.graphql.client.impl.core;

public class EnumImpl extends AbstractEnum {

    @Override
    public String build() {
        validateValue(this.getValue());
        return this.getValue();
    }

}
