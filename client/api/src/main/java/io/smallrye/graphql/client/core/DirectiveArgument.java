package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.DirectiveArgumentFactory;

public interface DirectiveArgument extends Buildable {
    static List<DirectiveArgument> directiveArgs(DirectiveArgument... directiveArgs) {
        return asList(directiveArgs);
    }

    static DirectiveArgument directiveArg(String name, Object value) {
        DirectiveArgument directiveArgument = getNewInstanceFromFactory(DirectiveArgumentFactory.class);

        directiveArgument.setName(name);
        directiveArgument.setValue(value);

        return directiveArgument;
    }

    String getName();

    void setName(String name1);

    Object getValue();

    void setValue(Object value);
}
