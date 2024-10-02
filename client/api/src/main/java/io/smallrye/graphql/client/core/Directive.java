package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.DirectiveFactory;

public interface Directive extends Buildable {
    static List<Directive> directives(Directive... directives) {
        return asList(directives);
    }

    static Directive directive(String name) {
        Directive directive = getNewInstanceFromFactory(DirectiveFactory.class);

        directive.setName(name);
        directive.setDirectiveArguments(emptyList());

        return directive;
    }

    static Directive directive(String name, DirectiveArgument... directiveArguments) {
        Directive directive = getNewInstanceFromFactory(DirectiveFactory.class);

        directive.setName(name);
        directive.setDirectiveArguments(asList(directiveArguments));

        return directive;
    }

    String getName();

    void setName(String name);

    List<DirectiveArgument> getDirectiveArguments();

    void setDirectiveArguments(List<DirectiveArgument> directiveArguments);
}
