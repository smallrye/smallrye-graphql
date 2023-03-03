package io.smallrye.graphql.client.impl.core;

import java.util.List;

import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.DirectiveArgument;

public abstract class AbstractDirective implements Directive {
    private String name;
    private List<DirectiveArgument> directiveArguments;

    public AbstractDirective() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DirectiveArgument> getDirectiveArguments() {
        return directiveArguments;
    }

    public void setDirectiveArguments(List<DirectiveArgument> directiveArguments) {
        this.directiveArguments = directiveArguments;
    }

}
